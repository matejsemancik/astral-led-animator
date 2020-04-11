package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import ddf.minim.ugens.Waves
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class LaserGenerator(sketch: PApplet, w: Int, h: Int, private val sink: Sink) : Generator {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)
    private val oscil = Oscil(1f, 1f, Waves.SAW).apply { patch(sink) }
    val frequency = Summer().apply { patch(oscil.frequency) }
    val amplitude = Summer().apply { patch(oscil.amplitude) }
    val beamWidth = Summer().apply { patch(sink) }
    val hue = Summer().apply { patch(sink) }
    val fading = Summer().apply { patch(sink) }
    val mod = Summer().apply { patch(sink) }

    override fun unpatch() {
        arrayOf(oscil, beamWidth, hue, fading, mod).forEach {
            it.unpatch(sink)
        }
    }

    fun generate(): PGraphics {
        val beamWidth = beamWidth.value.toInt().constrain(low = 1)
        val mod = mod.value.toInt().constrain(low = 1)

        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            pushPop {
                noFill()
                stroke(color(hue.value, 100f, 100f))
                strokeWeight(1f)

                fadeToBlackBy(fading.value)
                val x = oscil.value.mapSin(0f, width.toFloat()).toInt()
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