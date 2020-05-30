package dev.matsem.ala

import dev.matsem.ala.tools.extensions.colorModeHSB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import processing.core.PApplet
import processing.core.PConstants
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class LiveSketch : PApplet(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private lateinit var engine: ScriptEngine

    override fun settings() {
        size(600, 400, PConstants.P2D)
    }

    @ExperimentalTime
    override fun setup() {
        colorModeHSB()
        surface.apply {
            setTitle("Live KTS demo")
            setResizable(true)
            setAlwaysOnTop(true)
        }

        engine = ScriptEngineManager().getEngineByExtension("kts")

        launch {
            engine.eval("val x = 4")
            val res2 = engine.eval("x + 5")
            println(res2)
        }
    }

    override fun draw() {
        background(100f, 100f, 100f)
    }
}