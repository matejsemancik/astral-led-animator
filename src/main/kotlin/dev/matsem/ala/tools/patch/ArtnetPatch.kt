package dev.matsem.ala.tools.patch

// Pixel consists of 3 channels (R, G, B)
inline class Pixel(private val value: Triple<Int, Int, Int>) {
    override fun toString() =
        value.toList().joinToString(separator = ",") { it.toString().padStart(3, '0') }.let { "[$it]" }
}

inline class UniverseToPixel(private val value: Pair<Int, Pixel>) {
    override fun toString() = "${value.first}: ${value.second}"
}

class ArtnetPatch(width: Int, height: Int) {

    enum class Direction {
        /**
         * Starts at bottom-left corner of matrix and makes snake pattern starting in right direction, upwards
         */
        SNAKE_NE
    }

    val patchMatrix = Array(height) {
        Array<UniverseToPixel?>(width) {
            null
        }
    }

    /**
     * Patches portion of patch matrix selected by [rangeX] and [rangeY] closed ranges using specified [direction].
     * All patched pixels are on specified [universe] and channels are auto-incremented in process, starting
     * at [startChannel].
     */
    fun patch(rangeX: IntRange, rangeY: IntRange, direction: Direction, universe: Int, startChannel: Int) {
        var chan = startChannel
        when (direction) {
            Direction.SNAKE_NE -> {
                val mod = rangeY.last % 2
                rangeY.reversed().forEach { y ->
                    if (y % 2 == mod) {
                        // from start to end
                        rangeX.forEach { x ->
                            patchMatrix[y][x] = UniverseToPixel(universe to Pixel(Triple(chan, chan + 1, chan + 2)))
                            chan += 3
                        }
                    } else {
                        // from end to start
                        rangeX.reversed().forEach { x ->
                            patchMatrix[y][x] = UniverseToPixel(universe to Pixel(Triple(chan, chan + 1, chan + 2)))
                            chan += 3
                        }
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