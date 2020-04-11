package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PGraphics

class KnightRiderGenerator(sketch: PApplet, sink: Sink, w: Int, h: Int) :
    BaseGenerator(sketch, sink, w, h) {

    private val oscil = Oscil(1f, 1f, Waves.TRIANGLE).sinked()
    val frequency = Summer().patchedTo(oscil.frequency)
    val amplitude = Summer().patchedTo(oscil.amplitude)
    val beamWidth = Summer().sinked()
    val hue = Summer().sinked()
    val fading = Summer().sinked()

    override fun generate(): PGraphics {
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