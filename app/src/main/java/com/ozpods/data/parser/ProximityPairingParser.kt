package com.ozpods.data.parser
import com.ozpods.data.model.AirPodsDevice
import com.ozpods.data.model.AirPodsModel
import com.ozpods.data.model.BatteryInfo
import com.ozpods.data.model.EarDetection
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ProximityPairingParser @Inject constructor() {
    companion object {
        const val APPLE_COMPANY_ID = 0x004C
        private const val PROXIMITY_PAIRING_TYPE: Byte = 0x07
        private const val MIN_MESSAGE_LENGTH = 11
        private const val BATTERY_DISCONNECTED = 0x0F
    }
    fun parse(data: ByteArray, address: String, rssi: Int): AirPodsDevice? {
        if (data.size < MIN_MESSAGE_LENGTH) return null
        val offset = findProximityPairingOffset(data) ?: return null
        val remaining = data.size - offset
        if (remaining < MIN_MESSAGE_LENGTH) return null
        val length = data[offset + 1].toInt() and 0xFF
        if (remaining < length + 2) return null
        val pairingStatus = data[offset + 2].toInt() and 0xFF
        val isPaired = pairingStatus == 0x01
        val modelId = ((data[offset + 3].toInt() and 0xFF) shl 8) or
                (data[offset + 4].toInt() and 0xFF)
        val model = AirPodsModel.fromModelId(modelId)
        val statusByte = data[offset + 5].toInt() and 0xFF
        val isFlipped = (statusByte and 0x02) != 0
        val podsBattery = data[offset + 6].toInt() and 0xFF
        val caseBatteryByte = data[offset + 7].toInt() and 0xFF
        val podANibble = (podsBattery shr 4) and 0x0F
        val podBNibble = podsBattery and 0x0F
        val caseNibble = caseBatteryByte and 0x0F
        val leftNibble = if (isFlipped) podBNibble else podANibble
        val rightNibble = if (isFlipped) podANibble else podBNibble
        val battery = BatteryInfo(
            left = nibbleToBatteryPercent(leftNibble),
            right = nibbleToBatteryPercent(rightNibble),
            case = nibbleToBatteryPercent(caseNibble)
        )
        val earBitA = (statusByte and 0x08) != 0
        val earBitB = (statusByte and 0x02) != 0
        val earDetection = if (isFlipped) {
            EarDetection(leftInEar = earBitB, rightInEar = earBitA)
        } else {
            EarDetection(leftInEar = earBitA, rightInEar = earBitB)
        }
        val lidOpenCounter = data[offset + 8].toInt() and 0xFF
        val isLidOpen = lidOpenCounter % 2 != 0
        val color = data[offset + 9].toInt() and 0xFF
        return AirPodsDevice(
            address = address, model = model, rssi = rssi,
            isPaired = isPaired, battery = battery,
            earDetection = earDetection, isLidOpen = isLidOpen, color = color
        )
    }
    private fun findProximityPairingOffset(data: ByteArray): Int? {
        var i = 0
        while (i < data.size - 1) {
            val type = data[i]
            val len = data[i + 1].toInt() and 0xFF
            if (type == PROXIMITY_PAIRING_TYPE) return i
            i += len + 2
            if (len == 0) break
        }
        return null
    }
    private fun nibbleToBatteryPercent(nibble: Int): Int {
        if (nibble == BATTERY_DISCONNECTED) return BatteryInfo.UNAVAILABLE
        return (nibble * 10).coerceIn(0, 100)
    }
}
