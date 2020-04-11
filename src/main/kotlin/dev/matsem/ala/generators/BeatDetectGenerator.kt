package dev.matsem.ala.generators

import ddf.minim.AudioInput
import ddf.minim.analysis.BeatDetect
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import dev.matsem.ala.tools.audio.BeatListener
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

class BeatDetectGenerator(private val sketch: PApplet, w: Int, h: Int, private val lineIn: AudioInput, sink: Sink) :
    Generator {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    val dampening = Summer().apply { patch(sink) }
    val fading = Summer().apply { patch(sink) }
    val hue = Summer().apply { patch(sink) }

    private val beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate()).apply { setSensitivity(10) }
    private val beatListener = BeatListener(lineIn, beatDetect)

    override fun unpatch() = beatListener.unpatch()

    fun generate(): PGraphics {
        beatDetect.setSensitivity(dampening.value.mapp(10f, 300f).toInt())
        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            fadeToBlackBy(fading.value)
            rectMode(PConstants.CORNER)
            noStroke()
            fill(color(hue.value.mapp(0f, 360f), 100f, 100f))
            val rectW = width / beatDetect.detectSize()
            for (i in 0 until beatDetect.detectSize()) {
                if (beatDetect.isOnset(i)) {
                    rect(i * rectW.toFloat(), 0f, rectW.toFloat(), height.toFloat())
                }
            }
        }
        return canvas
    }
}