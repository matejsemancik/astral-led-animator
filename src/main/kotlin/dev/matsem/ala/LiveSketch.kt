package dev.matsem.ala

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.live.FileWatcher
import dev.matsem.ala.tools.live.ScriptLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File

class LiveSketch : PApplet() {

    private val scriptFile = File("src/main/kotlin/dev/matsem/ala/LiveScript.kts")
    private val scriptLoader = ScriptLoader()
    private var liveFunction: (PGraphics.() -> Unit)? = null

    private val fileWatcher: FileWatcher = FileWatcher()

    private lateinit var canvas: PGraphics

    override fun settings() {
        size(640, 480, PConstants.P2D)
    }

    override fun setup() {
        colorModeHSB()
        surface.apply {
            setTitle("Live KTS demo")
            setResizable(true)
            setAlwaysOnTop(true)
        }
        canvas = createGraphics(width, height, PConstants.P3D)
        loadScript()
        fileWatcher.watchFile(scriptFile) {
            println("🛰 File change detected")
            loadScript()
        }
    }

    override fun draw() {
        background(0f, 0f, 10f)
        liveFunction?.let { func ->
            func(canvas)
        }

        image(canvas, 0f, 0f)
    }

    override fun mouseClicked() = loadScript()

    private fun loadScript() {
        GlobalScope.launch(Dispatchers.Default) {
            liveFunction = scriptLoader.loadScript(scriptFile)
        }
    }
}