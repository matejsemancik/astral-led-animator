package dev.matsem.ala.generators

import ddf.minim.UGen
import ddf.minim.analysis.BeatDetect
import ddf.minim.ugens.Multiplier
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.audio.BeatListener
import dev.matsem.ala.tools.extensions.*
import dev.matsem.ala.tools.gameoflife.AliveCell
import dev.matsem.ala.tools.gameoflife.DeadCell
import dev.matsem.ala.tools.gameoflife.Universe
import kotlin.random.Random

object : BaseLiveGenerator() {

    override val enabled = true

    var frames = 0

    lateinit var universe: Universe
    lateinit var beatDetect: BeatDetect
    lateinit var beatListener: BeatListener

    lateinit var hue: UGen
    lateinit var coolingFactor: UGen
    lateinit var speed: UGen
    lateinit var randomizeThreshold: UGen

    override fun onPatch() {
        super.onPatch()
        universe = Universe(Array(canvas.height) {
            Array(canvas.width) {
                if (Random.nextFloat() > 0.5f) AliveCell else DeadCell
            }
        })
        beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate()).apply { setSensitivity(100) }
        beatListener = BeatListener(lineIn, beatDetect)

        hue = patchBox.knob1.patch(Multiplier(360f)).sinked()
        coolingFactor = patchBox.slider4.sinked()
        speed = patchBox.knob3.sinked()
        randomizeThreshold = patchBox.knob4.sinked()
    }

    override fun onUnpatch() {
        super.onUnpatch()
        beatListener.unpatch()
    }

    override fun generate(): GeneratorResult {
        frames++
        val frameSkip = speed.value.remap(0f, 1f, 10f, 1f).toInt()
        if (frames % frameSkip != 0) {
            return GeneratorResult(canvas, BlendMode.ADD)
        }

        if (beatDetect.isKick) {
            randomize(randomizeThreshold.value)
        }

        universe.coolingFactor = coolingFactor.value
        universe.nextGeneration()
        canvas.draw {
            colorModeHSB()
            loadPixels()
            universe.heatMap.forEachIndexed { y, cols ->
                cols.forEachIndexed { x, heat ->
                    setPixel(x, y, color(hue.value, 100f, heat * 100f))
                }
            }
            updatePixels()
        }

        return GeneratorResult(canvas, BlendMode.ADD)
    }

    private fun randomize(threshold: Float) {
        universe.cells.forEach {
            it.forEachIndexed { index, cell ->
                it[index] = if (Random.nextFloat() > threshold.constrain(0f, 1f)) AliveCell else cell
            }
        }
    }
}