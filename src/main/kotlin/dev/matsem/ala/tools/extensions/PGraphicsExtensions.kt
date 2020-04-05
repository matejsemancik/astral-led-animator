package dev.matsem.ala.tools.extensions

import processing.core.PGraphics
import kotlin.math.max
import kotlin.math.min

fun PGraphics.shorterDimension(): Int = min(width, height)

fun PGraphics.longerDimension(): Int = max(width, height)

fun PGraphics.centerX() = this.width / 2f

fun PGraphics.centerY() = this.height / 2f

fun PGraphics.translateCenter() = translate(centerX(), centerY())

fun PGraphics.pixelAt(x: Int, y: Int): Int = this.pixels[x + (y * width)]

fun PGraphics.pushPop(block: PGraphics.() -> Unit) {
    pushMatrix()
    this.block()
    popMatrix()
}

fun PGraphics.draw(block: PGraphics.() -> Unit) {
    beginDraw()
    this.block()
    endDraw()
}