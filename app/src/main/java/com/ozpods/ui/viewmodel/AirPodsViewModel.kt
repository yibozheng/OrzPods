package com.ozpods.ui.viewmodel

import android.app.Application
import android.content.Intent
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

    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true
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

    override fun onCleared() {
        stopScanning()
        super.onCleared()
    }
}
