package dev.matsem.ala.generators

import ddf.minim.AudioInput
import ddf.minim.UGen
import ddf.minim.ugens.Sink
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.live.PatchBox
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

abstract class BaseLiveGenerator {

    lateinit var sketch: PApplet
    lateinit var canvas: PGraphics
    lateinit var sink: Sink
    lateinit var lineIn: AudioInput
    lateinit var patchBox: PatchBox

    open val renderer = PConstants.P2D
    open val enabled: Boolean = true
    private val sinkedUGens = mutableListOf<UGen>()

    abstract fun generate(): GeneratorResult

    open fun init(sketch: PApplet, sink: Sink, lineIn: AudioInput, patchBox: PatchBox, w: Int, h: Int) {
        this.sketch = sketch
        this.canvas = sketch.createGraphics(w, h, renderer)
        this.sink = sink
        this.lineIn = lineIn
        this.patchBox = patchBox

        onPatch()
    }

    open fun onPatch() = Unit

    open fun onUnpatch() {
        sinkedUGens.forEach {
            it.unpatch(sink)
        }
    }

    /**
     * Patches this UGen to global [Sink] used to tick all running UGens.
     * This also registers this [UGen] to [sinkedUGens], all of which will be unpatched from [sink] after generator
     * is destroyed to avoid possible memory leaks.
     */
    fun <T : UGen> T.sinked() = apply {
        patch(sink)
        sinkedUGens.add(this)
    }
}