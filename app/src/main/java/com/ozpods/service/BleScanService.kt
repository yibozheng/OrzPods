package com.ozpods.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.ozpods.R
import com.ozpods.data.parser.ProximityPairingParser
import com.ozpods.data.repository.AirPodsRepository
import com.ozpods.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class BleScanService : Service() {
    @Inject lateinit var parser: ProximityPairingParser
    @Inject lateinit var repository: AirPodsRepository
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var scanner: BluetoothLeScanner? = null
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { handleScanResult(it) }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed with error code: $errorCode")
            val message = when (errorCode) {
                ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "Scan already in progress"
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed"
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE scan not supported"
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "Internal scan error"
                else -> "Scan failed (error $errorCode)"
            }
            repository.setScanError(message)
        }
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startPeriodicCleanup()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startScanning()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        stopScanning()
        scope.cancel()
        super.onDestroy()
    }
    private fun startScanning() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        scanner = bluetoothManager?.adapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "BluetoothLeScanner not available")
            repository.setScanError("Bluetooth scanner not available")
            stopSelf()
            return
        }
        val scanFilter = ScanFilter.Builder()
            .setManufacturerData(
                ProximityPairingParser.APPLE_COMPANY_ID,
                byteArrayOf(0x07), byteArrayOf(0xFF.toByte())
            ).build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        try {
            scanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
            Log.i(TAG, "BLE scanning started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing BLE permissions", e)
            repository.setScanError("Missing Bluetooth permissions")
            stopSelf()
        }
    }
    private fun stopScanning() {
        try { scanner?.stopScan(scanCallback) }
        catch (e: SecurityException) { Log.w(TAG, "Could not stop scan", e) }
        scanner = null
    }
    private fun handleScanResult(result: ScanResult) {
        val scanRecord = result.scanRecord ?: return
        val appleData = scanRecord.getManufacturerSpecificData(
            ProximityPairingParser.APPLE_COMPANY_ID) ?: return
        val device = parser.parse(appleData, result.device.address, result.rssi) ?: return
        repository.updateDevice(device)
    }
    private fun startPeriodicCleanup() {
        scope.launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL_MS)
                repository.removeStaleDevices()
            }
        }
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID,
            getString(R.string.scan_notification_channel),
            NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.scan_notification_title))
            .setContentText(getString(R.string.scan_notification_text))
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    companion object {
        private const val TAG = "BleScanService"
        private const val CHANNEL_ID = "ble_scan_channel"
        private const val NOTIFICATION_ID = 1
        private const val CLEANUP_INTERVAL_MS = 10_000L
        const val ACTION_START = "com.ozpods.action.START_SCAN"
        const val ACTION_STOP = "com.ozpods.action.STOP_SCAN"
        fun startIntent(context: Context): Intent =
            Intent(context, BleScanService::class.java).apply { action = ACTION_START }
        fun stopIntent(context: Context): Intent =
            Intent(context, BleScanService::class.java).apply { action = ACTION_STOP }
    }
}
