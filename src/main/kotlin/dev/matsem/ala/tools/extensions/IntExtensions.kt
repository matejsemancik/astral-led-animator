package dev.matsem.ala.tools.extensions

import processing.core.PApplet

fun Int.midiRange(start: Float, end: Float): Float {
    return PApplet.map(this.toFloat(), 0f, 127f, start, end)
}

fun Int.midiRange(top: Float): Float {
    return this.midiRange(0f, top)
}

fun Int.remap(start1: Float, end1: Float, start2: Float, end2: Float): Float =
    PApplet.map(this.toFloat(), start1, end1, start2, end2)

fun Int.constrain(low: Int = Int.MIN_VALUE, high: Int = Int.MAX_VALUE): Int = PApplet.constrain(this, low, high)

fun Int.toMidi(low: Int, high: Int): Int = PApplet.map(this.toFloat(), low.toFloat(), high.toFloat(), 0f, 127f).toInt()

inline val Int.rgbRed get() = this shr 16 and 0xff

inline val Int.rgbGreen get() = this shr 8 and 0xff

inline val Int.rgbBlue get() = this and 0xff

/**
 * Sets contrast to a single pixel.
 * [contrast] reasonable in [0f..5f]
 * [brightness] reasonable in [-128f..128f]
 */
fun Int.contrast(contrast: Float, brightness: Float): Int {
    val r = (rgbRed * contrast + brightness).toInt().constrain(0, 255)
    val g = (rgbGreen * contrast + brightness).toInt().constrain(0, 255)
    val b = (rgbBlue * contrast + brightness).toInt().constrain(0, 255)
    return (0xff shl 24) or (r shl 16) or (g shl 8) or b
}