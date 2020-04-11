package dev.matsem.ala.generators

import ddf.minim.AudioInput
import ddf.minim.analysis.FFT
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import dev.matsem.ala.tools.audio.FFTListener
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.math.max

class FFTGenerator(
    sketch: PApplet,
    w: Int,
    h: Int,
    lineIn: AudioInput,
    private val sink: Sink
) : Generator {

    private val canvas = sketch.createGraphics(w, h, PConstants.P2D)

    val fading = Summer().apply { patch(sink) }
    val hue = Summer().apply { patch(sink) }
    val widthSpan = Summer().apply { patch(sink) }
    val mirroring = Summer().apply { patch(sink) }

    private val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate()).apply {
        logAverages(22, 3)
    }
    private val fftListener = FFTListener(lineIn, fft)
    private val maximums = Array(h) { 0f }
    private val smoothed = Array(h) { 0f }

    override fun unpatch() {
        fftListener.unpatch()
        fading.unpatch(sink)
        hue.unpatch(sink)
        widthSpan.unpatch(sink)
        mirroring.unpatch(sink)
    }

    fun generate(): PGraphics {
        val freqRange = 20f..1000f
        val ranges = freqRange.split(canvas.height)
        val averages = ranges.map { range -> fft.calcAvg(range) }
        smoothed.modifyIndexed { index, current -> (current + averages[index]) / 2f }
        maximums.modifyIndexed { index, current -> max(current, smoothed[index]) }
        val widths = smoothed.zip(maximums).map { (avg, max) ->
            avg.remap(0f, max, 0f, canvas.width * widthSpan.value)
        }

        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            fadeToBlackBy(fading.value)
            noFill()
            strokeWeight(1f)
            stroke(color(hue.value, 100f, 100f))

            for (y in 0 until widths.count()) {
                line(0f, y.toFloat(), widths[y], y.toFloat())
            }

            if (mirroring.value > 0.5f) {
                loadPixels()
                val mirrored = pixels.copyOf().reversed().toTypedArray()
                mirrored.modifyIndexed { index, current ->
                    current or pixels[index]
                }

                pixels = mirrored.toIntArray()
                updatePixels()
            }
        }
        return canvas
    }
}