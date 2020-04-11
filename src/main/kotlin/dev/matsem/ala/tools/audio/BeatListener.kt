package dev.matsem.ala.tools.audio

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.analysis.BeatDetect

class BeatListener(private val lineIn: AudioInput, private val beatDetect: BeatDetect) : AudioListener {

    init {
        lineIn.addListener(this)
    }

    fun unpatch() = lineIn.removeListener(this)

    override fun samples(samp: FloatArray?) = beatDetect.detect(lineIn.mix)

    override fun samples(sampL: FloatArray?, sampR: FloatArray?) = beatDetect.detect(lineIn.mix)
}