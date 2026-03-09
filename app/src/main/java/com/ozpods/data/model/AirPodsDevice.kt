package com.ozpods.data.model
data class AirPodsDevice(
    val address: String,
    val model: AirPodsModel,
    val rssi: Int,
    val isPaired: Boolean,
    val battery: BatteryInfo,
    val earDetection: EarDetection,
    val isLidOpen: Boolean,
    val color: Int,
    val lastSeen: Long = System.currentTimeMillis()
) {
    val isNearby: Boolean
        get() = rssi > -70
    val isStale: Boolean
        get() = System.currentTimeMillis() - lastSeen > STALE_TIMEOUT_MS
    companion object {
        const val STALE_TIMEOUT_MS = 30_000L
    }
}
data class BatteryInfo(
    val left: Int = UNAVAILABLE,
    val right: Int = UNAVAILABLE,
    val case: Int = UNAVAILABLE
) {
    val isAnyAvailable: Boolean
        get() = left != UNAVAILABLE || right != UNAVAILABLE || case != UNAVAILABLE
    companion object {
        const val UNAVAILABLE = -1
    }
}
data class EarDetection(
    val leftInEar: Boolean = false,
    val rightInEar: Boolean = false
) {
    val bothInEar: Boolean get() = leftInEar && rightInEar
    val eitherInEar: Boolean get() = leftInEar || rightInEar
}
