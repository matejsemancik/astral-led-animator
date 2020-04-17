package dev.matsem.ala.ui.patch

data class PatchStyle(
    val paddingTop: Int = 10,
    val paddingBottom: Int = 10,
    val textMargin: Int = 4,
    val portSize: Int = 8,
    val bgColor: Int = 0x181818 or (0xff shl 24),
    val idleColor: Int = 0xe0e0e0 or (0xff shl 24),
    val activeColor: Int = 0xff00ff or (0xff shl 24)
)