package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.properties.Delegates

class KnightRiderGenerator(private val sketch: PApplet, w: Int, h: Int, private val sink: Sink) : Generator {

    private var fHzInternal: Float by Delegates.observable(0f) { _, old, new ->
        if (new != old) {
            oscil.setFrequency(new)
        }
    }
    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)
    private val oscil = Oscil(fHzInternal, 1f, Waves.SINE).apply { patch(sink) }

    override fun destroy() = oscil.unpatch(sink)

    fun generate(fHz: Float, beamWidth: Int, color: Int, fading: Float): PGraphics {
        fHzInternal = fHz
        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            pushPop {
                noStroke()
                fill(color)

                fadeToBlackBy(fading)
                println(oscil.lastValue)
                val x = oscil.lastValue.mapSin(0f, (width - (beamWidth - 1).toFloat())).toInt()
                rect(x.toFloat(), 0f, beamWidth.toFloat(), height.toFloat())
            }
        }

        return canvas
    }
}