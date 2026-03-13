package com.ozpods.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ozpods.data.model.AirPodsDevice
import com.ozpods.data.repository.AirPodsRepository
import com.ozpods.service.BleScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AirPodsViewModel @Inject constructor(
    private val application: Application,
    private val repository: AirPodsRepository
) : AndroidViewModel(application) {

    val devices: StateFlow<List<AirPodsDevice>> = repository.devices
        .map { devicesMap ->
            devicesMap.values
                .filter { !it.isStale }
                .sortedByDescending { it.rssi }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _bluetoothEnabled = MutableStateFlow(isBluetoothOn())
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()

    val scanError: StateFlow<String?> = repository.scanError

    // 蓝牙关闭前是否在扫描，用于蓝牙重新开启时自动恢复
    private var wasScanningBeforeBtOff = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        _bluetoothEnabled.value = false
                        wasScanningBeforeBtOff = _isScanning.value
                        if (_isScanning.value) stopScanning()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        _bluetoothEnabled.value = true
                        repository.setScanError(null)
                        if (wasScanningBeforeBtOff) startScanning()
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(bluetoothReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            application.registerReceiver(bluetoothReceiver, filter)
        }
    }

    fun startScanning() {
        if (_isScanning.value) return
        if (!_bluetoothEnabled.value) return
        _isScanning.value = true
        repository.setScanError(null)
        val intent = BleScanService.startIntent(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    fun stopScanning() {
        if (!_isScanning.value) return
        _isScanning.value = false
        application.startService(BleScanService.stopIntent(application))
    }

    fun toggleScanning() {
        if (_isScanning.value) stopScanning() else startScanning()
    }

    fun refresh() {
        repository.clear()
        stopScanning()
        startScanning()
    }

    fun dismissError() {
        repository.setScanError(null)
    }

    fun getDevice(address: String): StateFlow<AirPodsDevice?> =
        repository.devices
            .map { it[address] }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun isBluetoothOn(): Boolean {
        val btManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return btManager?.adapter?.isEnabled == true
    }

    override fun onCleared() {
        application.unregisterReceiver(bluetoothReceiver)
        stopScanning()
        super.onCleared()
    }
}
