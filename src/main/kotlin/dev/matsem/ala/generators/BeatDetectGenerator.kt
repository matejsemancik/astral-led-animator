package dev.matsem.ala.generators

import ddf.minim.AudioInput
import ddf.minim.analysis.BeatDetect
import dev.matsem.ala.tools.audio.BeatListener
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.fadeToBlackBy
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.properties.Delegates

class BeatDetectGenerator(private val sketch: PApplet, w: Int, h: Int, private val lineIn: AudioInput) : Generator {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    var dampening: Int by Delegates.observable(300) { _, old, new ->
        if (new != old) {
            beatDetect.setSensitivity(new)
        }
    }
    var fading = 1f
    var color: Int = sketch.color(120f, 100f, 100f)
    private val beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate()).apply { setSensitivity(dampening) }
    private val beatListener = BeatListener(lineIn, beatDetect)

    override fun unpatch() = beatListener.unpatch()

    fun generate(): PGraphics {
        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            fadeToBlackBy(fading)
            rectMode(PConstants.CORNER)
            noStroke()
            fill(color)
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