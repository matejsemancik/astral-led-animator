package dev.matsem.ala.ui.patch

import processing.core.PFont

data class PatchStyle(
    val paddingVertical: Int = 10,
    val bgColor: Int = 0x242424 or (0xff shl 24),
    val idleColor: Int = 0xe0e0e0 or (0xff shl 24),
    val activeColor: Int = 0xffffff or (0xff shl 24),
    val font: PFont
)