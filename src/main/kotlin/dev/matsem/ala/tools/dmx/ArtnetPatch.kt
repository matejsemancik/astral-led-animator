package dev.matsem.ala.tools.dmx

import dev.matsem.ala.tools.extensions.pixelAt
import dev.matsem.ala.tools.extensions.rgbBlue
import dev.matsem.ala.tools.extensions.rgbGreen
import dev.matsem.ala.tools.extensions.rgbRed
import processing.core.PGraphics

/**
 * Represents a single pixel on a fixture.
 */
inline class PixelChannels(val value: Triple<Int, Int, Int>) {
    override fun toString() =
        value.toList().joinToString(separator = ",") { it.toString().padStart(3, '0') }.let { "[$it]" }
}

/**
 * Maps pixel on a canvas to ArtNet universe.
 */
inline class UniverseToPixelChannels(val value: Pair<Int, PixelChannels>) {
    override fun toString() = "${value.first}: ${value.second}"
}

class ArtnetPatch(val patchWidth: Int, val patchHeight: Int) {

    enum class Direction {
        /**
         * Starts at bottom-left corner of matrix and makes snake pattern starting in right direction, upwards
         */
        SNAKE_NE
    }

    private val patchMatrix = Array(patchHeight) {
        Array<UniverseToPixelChannels?>(patchWidth) { null }
    }

    val universeData = HashMap<Int, ByteArray>()

    /**
     * Patches portion of patch matrix selected by [rangeX] and [rangeY] closed ranges using specified [direction].
     * All patched pixels are on specified [universe] and channels are auto-incremented in process, starting
     * at [startChannel].
     */
    fun patch(rangeX: IntRange, rangeY: IntRange, direction: Direction, universe: Int, startChannel: Int) {
        check(rangeX.count() * rangeY.count() * 3 <= 512) { "Patch is out of range" }
        var chan = startChannel
        when (direction) {
            Direction.SNAKE_NE -> {
                val mod = rangeY.last % 2
                rangeY.reversed().forEach { y ->
                    if (y % 2 == mod) {
                        // from start to end
                        rangeX.forEach { x ->
                            patchMatrix[y][x] =
                                UniverseToPixelChannels(universe to PixelChannels(Triple(chan, chan + 1, chan + 2)))
                            chan += 3
                        }
                    } else {
                        // from end to start
                        rangeX.reversed().forEach { x ->
                            patchMatrix[y][x] =
                                UniverseToPixelChannels(universe to PixelChannels(Triple(chan, chan + 1, chan + 2)))
                            chan += 3
                        }
                    }
                }
            }
        }
    }

    /**
     * Scans supplied [graphics] image buffer and converts it to raw ArtNet data ByteArrays, mapped according to
     * [patchMatrix]. For example, if pixel[x = 0, y = 0] is patched to universe 0 and channels 10, 11 and 12,
     * the data ByteArray for universe 0 will have this pixel's data at data[10] = R, data[11] = G, data[12] = B.
     */
    fun scan(graphics: PGraphics) = with(graphics) {
        loadPixels()
        for (y in 0 until height) {
            for (x in 0 until width) {
                patchMatrix[y][x]?.value?.let { (universe, channels) ->
                    universeData
                        .getOrPut(universe) { ByteArray(512) { 0.toByte() } }
                        .apply {
                            val pixel = pixelAt(x, y)
                            set(channels.value.first, pixel.rgbRed.toByte())
                            set(channels.value.second, pixel.rgbGreen.toByte())
                            set(channels.value.third, pixel.rgbBlue.toByte())
                        }
                }
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        patchMatrix.forEach { row ->
            sb.append("| ")
            row.forEach {
                sb.append(it?.toString() ?: "----------------")
                sb.append(" | ")
            }
            sb.append("\n")
        }

        return sb.toString()
    }
}