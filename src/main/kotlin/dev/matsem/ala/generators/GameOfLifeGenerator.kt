package dev.matsem.ala.generators

import ddf.minim.AudioInput
import ddf.minim.analysis.BeatDetect
import ddf.minim.ugens.Multiplier
import ddf.minim.ugens.Sink
import ddf.minim.ugens.Summer
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.audio.BeatListener
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.constrain
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.remap
import dev.matsem.ala.tools.extensions.setPixel
import dev.matsem.ala.tools.extensions.value
import processing.core.PApplet
import kotlin.random.Random

class GameOfLifeGenerator(
    sketch: PApplet,
    sink: Sink,
    w: Int,
    h: Int,
    lineIn: AudioInput
) : BaseGenerator(sketch, sink, w, h) {

    private val universe = Universe(Array(h) { Array(w) { if (Random.nextFloat() > 0.5f) AliveCell else DeadCell } })
    val beatDetect = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate())
        .apply { setSensitivity(300) }
        .also { BeatListener(lineIn, it) }

    var frames = 0
    val hue = Summer().patch(Multiplier(360f)).sinked()
    val coolingFactor = Summer().sinked()
    val speed = Summer().sinked()
    val randomizeThrehold = Summer().sinked()

    override fun generate(): GeneratorResult {
        frames++
        val frameSkip = speed.value.remap(0f, 1f, 10f, 1f).toInt()
        if (frames % frameSkip != 0) {
            return GeneratorResult(canvas, BlendMode.ADD)
        }

        if (beatDetect.isKick) {
            randomize(randomizeThrehold.value)
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

sealed class Cell {

    companion object {
        const val CHARACTER_ALIVE = '*'
        const val CHARACTER_DEAD = '.'

        fun from(char: Char) = if (char == CHARACTER_ALIVE) AliveCell else DeadCell
    }

    abstract fun character(): Char
}

object AliveCell : Cell() {
    override fun character() = CHARACTER_ALIVE
}

object DeadCell : Cell() {
    override fun character() = CHARACTER_DEAD
}

class Universe(
    var cells: Array<Array<Cell>>
) {

    val width: Int
        get() = cells[0].size

    val height: Int
        get() = cells.size

    val heatMap: Array<Array<Float>> = Array(height) { y ->
        Array(width) { x ->
            if (cells[y][x] is AliveCell) 1f else 0f
        }
    }

    var coolingFactor = 0.90f

    fun nextGeneration() {
        val nextGeneration = Array(height) { Array<Cell>(width) { DeadCell } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val neighborCount = neighborCount(y, x)

                nextGeneration[y][x] = when {
                    cells[y][x] is AliveCell && neighborCount in 2..3 -> AliveCell
                    cells[y][x] is DeadCell && neighborCount == 3 -> AliveCell
                    else -> DeadCell
                }

                heatMap[y][x] = when {
                    cells[y][x] is AliveCell -> 1f
                    else -> heatMap[y][x] * coolingFactor
                }
            }
        }

        cells = nextGeneration
    }

    private fun neighborCount(atY: Int, atX: Int): Int {
        var count = 0
        for (y in (atY - 1)..(atY + 1)) {
            for (x in (atX - 1)..(atX + 1)) {
                // If out of bounds
                if (y < 0 || x < 0 || y >= height || x >= width) {
                    continue
                }

                // If this cell
                if (y == atY && x == atX) {
                    continue
                }

                if (cells[y][x] is AliveCell) {
                    count++
                }
            }
        }

        return count
    }

    override fun toString(): String {
        return cells.joinToString(
            separator = "\n",
            transform = { row ->
                row.joinToString(
                    separator = "",
                    transform = { cell ->
                        "${cell.character()}"
                    }
                )
            }
        )
    }
}