package dev.matsem.ala.tools.live

import ddf.minim.AudioInput
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.midiRange
import dev.matsem.ala.tools.kontrol.KontrolF1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File

class GeneratorLiveScript(
    private val file: File,
    private val sketch: PApplet,
    private val sink: Sink,
    private val lineIn: AudioInput,
    private val w: Int,
    private val h: Int
) {

    private val lock = Any()
    private var script: BaseLiveGenerator? = null
    private val ui: PGraphics = sketch.createGraphics(w * 2, h * 2 + 16, PConstants.P2D)
    private val patchBox: PatchBox = PatchBox()

    var isEnabled: Boolean = true
    var result: GeneratorResult? = null
    private var dirty: Boolean = false

    fun reload() {
        dirty = true
        GlobalScope.launch(Dispatchers.Default) {
            val loadedGen = ScriptLoader().loadScript<BaseLiveGenerator>(file)
            loadedGen.init(sketch, sink, lineIn, patchBox, w, h)
            synchronized(lock) {
                safeEval {
                    script?.onUnpatch()
                }

                script = loadedGen
            }

            dirty = false
        }
    }

    fun updatePatchBox(kontrol: KontrolF1) {
        patchBox.knob1.setConstant(kontrol.knob1.midiRange(1f))
        patchBox.knob2.setConstant(kontrol.knob2.midiRange(1f))
        patchBox.knob3.setConstant(kontrol.knob3.midiRange(1f))
        patchBox.knob4.setConstant(kontrol.knob4.midiRange(1f))
        patchBox.slider1.setConstant(kontrol.slider1.midiRange(1f))
        patchBox.slider2.setConstant(kontrol.slider2.midiRange(1f))
        patchBox.slider3.setConstant(kontrol.slider3.midiRange(1f))
        patchBox.slider4.setConstant(kontrol.slider4.midiRange(1f))
    }

    fun update() {
        if (isEnabled) {
            safeEval {
                result = script?.generate()
            }
        }
    }

    fun unload() {
        safeEval {
            script?.onUnpatch()
        }
    }

    fun generateUi(): PGraphics {
        ui.draw {
            colorModeHSB()
            clear()

            val titleColor = if (dirty) {
                color(20f, 100f, 100f)
            } else {
                color(0f, 0f, 100f)
            }
            textSize(12f)
            noStroke()
            fill(titleColor)
            text(file.name, 0f, 12f)
            result?.let {
                image(it.graphics, 0f, 16f, w * 2f, h * 2f)
            }
        }

        return ui
    }
}

private fun <T> safeEval(block: () -> T) {
    try {
        block()
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}