package dev.matsem.ala.generators

import ddf.minim.UGen
import ddf.minim.analysis.BeatDetect
import ddf.minim.ugens.Multiplier
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.audio.BeatListener
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.fadeToBlackBy
import dev.matsem.ala.tools.extensions.value
import processing.core.PConstants

object : BaseLiveGenerator() {

    override val enabled = true

    lateinit var fading: UGen
    lateinit var hue: UGen

    lateinit var beatDetect: BeatDetect
    lateinit var beatListener: BeatListener

    override fun onPatch() {
        super.onPatch()

        hue = patchBox.knob1.patch(Multiplier(360f)).sinked()
        fading = patchBox.knob2.sinked()


        beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate()).apply { setSensitivity(300) }
        beatListener = BeatListener(lineIn, beatDetect)
    }

    override fun onUnpatch() {
        super.onUnpatch()
        beatListener.unpatch()
    }

    override fun generate(): GeneratorResult {
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