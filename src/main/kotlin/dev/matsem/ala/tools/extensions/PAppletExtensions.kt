package dev.matsem.ala.tools.extensions

import processing.core.PApplet
import processing.core.PConstants
import kotlin.math.max
import kotlin.math.min

fun PApplet.centerX() = this.width / 2f

fun PApplet.centerY() = this.height / 2f

fun PApplet.translateCenter() = translate(centerX(), centerY())

fun PApplet.shorterDimension(): Int = min(width, height)

fun PApplet.longerDimension(): Int = max(width, height)

/**
 * Generates saw signal with given frequency in range from 0f to 1f
 */
fun PApplet.saw(fHz: Float, offset: Int = 0): Float = ((millis() - offset) % (1000f * 1 / fHz)) / (1000f * 1 / fHz)

fun PApplet.radianS(periodSeconds: Float) = (millis() / 1000f * PConstants.TWO_PI / periodSeconds) % PConstants.TWO_PI

fun PApplet.radianHz(hz: Float) =
    ((millis() / 1000f * PConstants.TWO_PI / (1f / hz)) % PConstants.TWO_PI) % PConstants.TWO_PI

fun PApplet.pushPop(block: PApplet.() -> Unit) {
    push()
    this.block()
    pop()
}

fun PApplet.colorModeHSB() = colorMode(PApplet.HSB, 360f, 100f, 100f, 100f)