package dev.matsem.ala.tools.live

import ddf.minim.AudioInput
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.GeneratorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import processing.core.PApplet
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

    var isEnabled: Boolean = true
    var result: GeneratorResult? = null

    fun reload() {
        GlobalScope.launch(Dispatchers.Default) {
            val loadedGen = ScriptLoader().loadScript<BaseLiveGenerator>(file)
            loadedGen.init(sketch, sink, lineIn, patchBox, w, h)
            synchronized(lock) {
                safeEval {
                    script?.onUnpatch()
                }

                script = loadedGen
            }
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
}

private fun <T> safeEval(block: () -> T) {
    try {
        block()
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}