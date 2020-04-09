package dev.matsem.ala.generators

import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class KnightRiderGenerator(private val sketch: PApplet, w: Int, h: Int) {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    fun generate(fHz: Float, beamWidth: Int, color: Int, fading: Float): PGraphics {
        canvas.noSmooth()
        canvas.draw {
            colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
            pushPop {
                rectMode(PConstants.CORNER)
                noStroke()
                fill(color)

                fadeToBlackBy(fading)
                val x = PApplet.sin(sketch.radianHz(fHz))
                    .mapSin(0f, (width - (beamWidth - 1)).toFloat())
                    .toInt()
                rect(x.toFloat(), 0f, beamWidth.toFloat(), height.toFloat())
            }
        }

        return canvas
    }
}