package com.ozpods.data.model
enum class AirPodsModel(val modelId: Int, val displayName: String, val hasAnc: Boolean = false) {
    AIRPODS_1(0x0220, "AirPods (1st Gen)"),
    AIRPODS_2(0x0F20, "AirPods (2nd Gen)"),
    AIRPODS_3(0x1320, "AirPods (3rd Gen)"),
    AIRPODS_4(0x1920, "AirPods (4th Gen)"),
    AIRPODS_4_ANC(0x1B20, "AirPods 4 (ANC)", hasAnc = true),
    AIRPODS_PRO(0x0E20, "AirPods Pro", hasAnc = true),
    AIRPODS_PRO_2_LIGHTNING(0x1420, "AirPods Pro 2", hasAnc = true),
    AIRPODS_PRO_2_USB_C(0x2420, "AirPods Pro 2 (USB-C)", hasAnc = true),
    AIRPODS_MAX_LIGHTNING(0x0A20, "AirPods Max", hasAnc = true),
    AIRPODS_MAX_USB_C(0x1F20, "AirPods Max (USB-C)", hasAnc = true),
    UNKNOWN(0x0000, "Unknown AirPods");
    companion object {
        private val modelMap = entries.associateBy { it.modelId }
        fun fromModelId(id: Int): AirPodsModel = modelMap[id] ?: UNKNOWN
    }
}
