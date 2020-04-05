package dev.matsem.ala.generators

import dev.matsem.ala.tools.extensions.angularTimeHz
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.mapSin
import dev.matsem.ala.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class KnightRiderGenerator(private val sketch: PApplet, w: Int, h: Int) {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    fun generate(fHz: Float, color: Int, fading: Float): PGraphics {
        canvas.noSmooth()
        canvas.draw {
            colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
            pushPop {
                noStroke()
                fill(0f, 0f, 0f, fading * 100f)
                rect(0f, 0f, width.toFloat(), height.toFloat())

                noFill()
                stroke(color)
                val x = PApplet.sin(sketch.angularTimeHz(fHz)).mapSin(0f, width.toFloat()).toInt().toFloat()
                line(x, 0f, x, height.toFloat())
            }
        }

        return canvas
    }
}