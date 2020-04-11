package dev.matsem.ala.generators

import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import ddf.minim.ugens.Waves
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet

class LaserGenerator(sketch: PApplet, sink: Sink, w: Int, h: Int) : BaseGenerator(sketch, sink, w, h) {

    private val oscil = Oscil(1f, 1f, Waves.SAW).sinked()
    val frequency = Summer().patchedTo(oscil.frequency)
    val amplitude = Summer().patchedTo(oscil.amplitude)
    val beamWidth = Summer().sinked()
    val hue = Summer().sinked()
    val fading = Summer().sinked()
    val mod = Summer().sinked()

    override fun generate(): GeneratorResult {
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

        return GeneratorResult(canvas, BlendMode.ADD)
    }
}