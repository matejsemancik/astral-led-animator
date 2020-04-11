package dev.matsem.ala.generators

import ddf.minim.UGen
import ddf.minim.ugens.Sink
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

abstract class BaseGenerator(sketch: PApplet, private val sink: Sink, w: Int, h: Int) {

    open val renderer = PConstants.P2D
    internal val canvas = sketch.createGraphics(w, h, renderer)
    private val sinkedUGens = mutableListOf<UGen>()

    abstract fun generate(): PGraphics

    fun unpatch() {
        sinkedUGens.forEach {
            it.unpatch(sink)
        }
    }

    /**
     * Patches this UGen to global [Sink] used to tick all running UGens, disposing any audio generated
     */
    internal fun <T : UGen> T.sinked() = apply {
        patch(sink)
        sinkedUGens.add(this)
    }

    /**
     * Patches [UGen] to provided [ugen] instance and returns this [UGen].
     * This method is different to UGen.patch(...) method, which, on other side returns [UGen]
     * being patched to for chaining purposes.
     */
    internal fun <T : UGen> T.patchedTo(ugen: UGen.UGenInput) = apply {
        patch(ugen)
    }
}