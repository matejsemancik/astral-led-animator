package dev.matsem.ala.tools.live

import ddf.minim.AudioInput
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
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
    private val patchBox: PatchBox,
    private val w: Int,
    private val h: Int
) {

    private val lock = Any()
    private var script: BaseLiveGenerator? = null
    private val ui: PGraphics = sketch.createGraphics(w * 2, h * 2 + 16, PConstants.P2D)

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