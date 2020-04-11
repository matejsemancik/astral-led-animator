package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.lastValue
import dev.matsem.ala.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.properties.Delegates

class StrobeGenerator(private val sketch: PApplet, w: Int, h: Int, private val sink: Sink) : Generator {

    private var fHzInternal: Float by Delegates.observable(0f) { _, old, new ->
        if (new != old) {
            oscil.setFrequency(new)
        }
    }
    private val oscil = Oscil(fHzInternal, 1f, Waves.SINE).apply { patch(sink) }
    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    override fun unpatch() = oscil.unpatch(sink)

    fun generate(fHz: Float, color: Int): PGraphics {
        fHzInternal = fHz
        val strobe = oscil.lastValue > 0 && fHz > 0f
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