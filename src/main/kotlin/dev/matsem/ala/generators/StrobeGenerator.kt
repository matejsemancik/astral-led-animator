package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.pushPop
import dev.matsem.ala.tools.extensions.value
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class StrobeGenerator(private val sketch: PApplet, w: Int, h: Int, private val sink: Sink) : Generator {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)
    private val oscil = Oscil(0f, 1f, Waves.SINE).apply { patch(sink) }
    val frequency = Summer().apply { patch(oscil.frequency) }

    override fun unpatch() {
        oscil.unpatch(sink)
    }

    fun generate(): PGraphics {
        val strobe = oscil.value > 0 && frequency.value > 0f
        canvas.draw {
            if (strobe) {
                colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
                pushPop {
                    rectMode(PConstants.CORNER)
                    noStroke()
                    fill(0)
                    rect(0f, 0f, width.toFloat(), height.toFloat())
                }
            } else {
                clear()
            }
        }

        return canvas
    }
}