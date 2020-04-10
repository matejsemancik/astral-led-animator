package dev.matsem.ala.generators

import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.pushPop
import dev.matsem.ala.tools.extensions.radianHz
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class StrobeGenerator(private val sketch: PApplet, w: Int, h: Int) {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    fun generate(fHz: Float, color: Int): PGraphics {
        val strobe = PApplet.sin(sketch.radianHz(fHz)) > 0
        canvas.draw {
            if (strobe) {
                colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
                pushPop {
                    rectMode(PConstants.CORNER)
                    noStroke()
                    fill(color)
                    rect(0f, 0f, width.toFloat(), height.toFloat())
                }
            } else {
                clear()
            }
        }

        return canvas
    }
}