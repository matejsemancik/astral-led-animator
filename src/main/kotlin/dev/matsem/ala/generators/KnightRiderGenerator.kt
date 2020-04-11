package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class KnightRiderGenerator(sketch: PApplet, w: Int, h: Int, private val sink: Sink) : Generator {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)
    private val oscil = Oscil(1f, 1f, Waves.TRIANGLE).apply { patch(sink) }
    val frequency = Summer().apply { patch(oscil.frequency) }
    val amplitude = Summer().apply { patch(oscil.amplitude) }
    val beamWidth = Summer().apply { patch(sink) }
    val hue = Summer().apply { patch(sink) }
    val fading = Summer().apply { patch(sink) }

    override fun unpatch() {
        oscil.unpatch(sink)
        beamWidth.unpatch(sink)
        hue.unpatch(sink)
        fading.unpatch(sink)
    }

    fun generate(): PGraphics {
        val beamWidth = beamWidth.value.toInt().constrain(low = 1)
        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            pushPop {
                noStroke()
                fill(color(hue.value, 100f, 100f))

                fadeToBlackBy(fading.value)
                val x = oscil.value.mapSin(0f, (width - (beamWidth - 1).toFloat())).toInt()
                rect(x.toFloat(), 0f, beamWidth.toFloat(), height.toFloat())
            }
        }

        return canvas
    }
}