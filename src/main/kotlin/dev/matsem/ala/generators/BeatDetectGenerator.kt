package dev.matsem.ala.generators

import ddf.minim.AudioInput
import ddf.minim.analysis.BeatDetect
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.audio.BeatListener
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.fadeToBlackBy
import dev.matsem.ala.tools.extensions.mapp
import dev.matsem.ala.tools.extensions.value
import processing.core.PApplet
import processing.core.PConstants

class BeatDetectGenerator(
    sketch: PApplet,
    sink: Sink,
    w: Int,
    h: Int,
    lineIn: AudioInput
) : BaseGenerator(sketch, sink, w, h) {

    val dampening = Summer().sinked()
    val fading = Summer().sinked()
    val hue = Summer().sinked()

    private val beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate())
        .apply { setSensitivity(10) }
        .also { BeatListener(lineIn, it) }

    override fun generate(): GeneratorResult {
        beatDetect.setSensitivity(dampening.value.mapp(10f, 300f).toInt())
        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            fadeToBlackBy(fading.value)
            rectMode(PConstants.CORNER)
            noStroke()
            fill(color(hue.value, 100f, 100f))
            val rectW = width / beatDetect.detectSize()
            for (i in 0 until beatDetect.detectSize()) {
                if (beatDetect.isOnset(i)) {
                    rect(i * rectW.toFloat(), 0f, rectW.toFloat(), height.toFloat())
                }
            }
        }

        return GeneratorResult(canvas, BlendMode.ADD)
    }
}