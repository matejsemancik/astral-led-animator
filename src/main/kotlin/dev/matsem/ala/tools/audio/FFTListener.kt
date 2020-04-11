package dev.matsem.ala.tools.audio

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.analysis.FFT

class FFTListener(private val lineIn: AudioInput, private val fft: FFT) : AudioListener {

    init {
        lineIn.addListener(this)
    }

    fun unpatch() = lineIn.removeListener(this)

    override fun samples(samp: FloatArray?) = fft.forward(lineIn.mix)

    override fun samples(sampL: FloatArray?, sampR: FloatArray?) = fft.forward(lineIn.mix)
}