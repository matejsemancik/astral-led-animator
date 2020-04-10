package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.properties.Delegates

class LaserGenerator(private val sketch: PApplet, w: Int, h: Int, private val sink: Sink) : Generator {

    private var fHzInternal: Float by Delegates.observable(0f) { _, old, new ->
        if (new != old) {
            oscil.setFrequency(new)
        }
    }
    private var amplitudeInternal: Float by Delegates.observable(1f) {_, old, new ->
        if (new != old) {
            oscil.setAmplitude(new)
        }
    }
    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)
    private val oscil = Oscil(fHzInternal, 1f, Waves.SAW).apply { patch(sink) }

    override fun destroy() = oscil.unpatch(sink)

    fun generate(fHz: Float, amplitude: Float, beamWidth: Int, color: Int, fading: Float, mod: Int): PGraphics {
        fHzInternal = fHz
        amplitudeInternal = amplitude

        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            pushPop {
                noFill()
                stroke(color)
                strokeWeight(1f)

                fadeToBlackBy(fading)
                val x = oscil.lastValue.mapSin(0f, width.toFloat()).toInt()
                for (y in 0 until height) {
                    if (y % mod == 0) {
                        line(x.toFloat(), y.toFloat(), x + beamWidth.toFloat(), y + 1f)
                    } else {
                        line(width - x.toFloat(), y.toFloat(), width - x - beamWidth.toFloat(), y + 1f)
                    }
                }
            }
        }

        return canvas
    }
}