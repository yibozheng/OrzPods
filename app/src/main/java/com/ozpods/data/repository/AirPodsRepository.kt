package com.ozpods.data.repository
import com.ozpods.data.model.AirPodsDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AirPodsRepository @Inject constructor() {
    private val _devices = MutableStateFlow<Map<String, AirPodsDevice>>(emptyMap())
    val devices: StateFlow<Map<String, AirPodsDevice>> = _devices.asStateFlow()
    fun updateDevice(device: AirPodsDevice) {
        _devices.update { current ->
            current.toMutableMap().apply { put(device.address, device) }
        }
    }
    fun removeStaleDevices() {
        _devices.update { current -> current.filterValues { !it.isStale } }
    }
    fun clear() { _devices.value = emptyMap() }
}
