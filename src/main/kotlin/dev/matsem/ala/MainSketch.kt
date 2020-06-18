package dev.matsem.ala

import ch.bildspur.artnet.ArtNetClient
import ddf.minim.Minim
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.tools.dmx.ArtnetPatch
import dev.matsem.ala.tools.extensions.*
import dev.matsem.ala.tools.kontrol.KontrolF1
import dev.matsem.ala.tools.live.FileWatcher
import dev.matsem.ala.tools.live.PatchBox
import dev.matsem.ala.tools.live.ScriptLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File
import kotlin.properties.Delegates

class MainSketch : PApplet() {

    object Config {
        const val LED_WIDTH = 60
        const val LED_HEIGHT = 2
        const val SPACE = 2
        const val SIZE = 10f
        const val NODE_IP = "192.168.1.18"
        const val OUTPUT_ENABLED = true
        const val GENERATORS_DIR = "src/main/kotlin/dev/matsem/ala/generators"
    }

    private var canvasWidth: Int by Delegates.vetoable(initialValue = Config.LED_WIDTH) { _, oldVal, newVal ->
        val hasChanged = oldVal != newVal
        if (hasChanged) {
            reloadGenerators(newVal, canvasHeight)
        }
        hasChanged
    }
    private var canvasHeight: Int by Delegates.vetoable(initialValue = Config.LED_HEIGHT) { _, oldVal, newVal ->
        val hasChanged = oldVal != newVal
        if (hasChanged) {
            reloadGenerators(canvasWidth, newVal)
        }
        hasChanged
    }

    // region Kontrol F1 MIDI controller stuff
    private val kontrol = KontrolF1().apply { connect() }
    // endregion

    // region Minim, oscillators, audio stuff
    private val minim = Minim(this)
    private val lineIn = minim.lineIn
    private val lineOut = minim.lineOut
    private val sink = Sink().apply { patch(lineOut) }
    private val patchBox = PatchBox()
    // endregion

    // region Art-Net stuff
    private val artnetClient = ArtNetClient().apply { start() }
    private val artnetPatch = ArtnetPatch(Config.LED_WIDTH, Config.LED_HEIGHT).apply {
        patch(0 until patchWidth, 0 until patchHeight, ArtnetPatch.Direction.SNAKE_NE, 0, 0)
    }
    // endregion

    // region Core stuff
    private lateinit var canvas: PGraphics
    private val lock = Any()
    private val generators = mutableMapOf<File, BaseLiveGenerator>()
    private val fileWatcher = FileWatcher()
    // endregion

    private fun reloadGenerators(w: Int, h: Int) {
        canvas = createGraphics(w, h, PConstants.P2D)

        println("Reloading generators @ ${w}x$h px")
        val liveScripts = File(Config.GENERATORS_DIR)
            .listFiles { file: File -> file.name.endsWith("kts") }
            ?.map { it.absoluteFile }
            ?: return

        if (liveScripts.isNotEmpty()) {
            println("Available files:\n ${liveScripts.joinToString(separator = "\n") { "\t${it.name}" }}")
        } else {
            println("No available files")
        }

        synchronized(lock) {
            generators.forEach { (_, gen) ->
                gen.onUnpatch()
            }
            generators.clear()
        }

        liveScripts.forEach { file ->
            loadScript(file, w, h)
        }
    }

    private fun loadScript(scriptFile: File, w: Int, h: Int) {
        GlobalScope.launch(Dispatchers.Default) {
            val loadedGen = ScriptLoader().loadScript<BaseLiveGenerator>(scriptFile)
            loadedGen.init(this@MainSketch, sink, lineIn, patchBox, w, h)
            synchronized(lock) {
                try {
                    generators[scriptFile]?.onUnpatch()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
                generators[scriptFile] = loadedGen
            }
        }
    }

    private fun unloadScript(scriptFile: File) {
        synchronized(lock) {
            generators[scriptFile]?.onUnpatch()
            generators.remove(scriptFile)
        }
        println("🗑 Script ${scriptFile.name} unloaded")
    }

    private fun watchFiles() {
        fileWatcher.watchPath(
            File(Config.GENERATORS_DIR).toPath(),
            onCreate = {
                println("💥 New script available: ${it.name}")
                loadScript(it, canvasWidth, canvasHeight)
            },
            onModify = {
                println("🔵 Script change detected: ${it.name}")
                loadScript(it, canvasWidth, canvasHeight)
            },
            onDelete = {
                println("Script removed: ${it.name}")
                unloadScript(it)
            }
        )
    }

    private fun updatePatchBox() {
        patchBox.knob1.setConstant(kontrol.knob1.midiRange(1f))
        patchBox.knob2.setConstant(kontrol.knob2.midiRange(1f))
        patchBox.knob3.setConstant(kontrol.knob3.midiRange(1f))
        patchBox.knob4.setConstant(kontrol.knob4.midiRange(1f))
        patchBox.slider1.setConstant(kontrol.slider1.midiRange(1f))
        patchBox.slider2.setConstant(kontrol.slider2.midiRange(1f))
        patchBox.slider3.setConstant(kontrol.slider3.midiRange(1f))
        patchBox.slider4.setConstant(kontrol.slider4.midiRange(1f))
    }

    override fun settings() = size(1280, 720, PConstants.P3D)

    override fun setup() {
        surface.setTitle("Astral LED Animator")
        surface.setResizable(true)
        surface.setAlwaysOnTop(true)
        surface.setSize(
            (canvasWidth * Config.SIZE + canvasWidth * Config.SPACE).toInt() + 200,
            (canvasHeight * Config.SIZE + canvasHeight * Config.SPACE).toInt() + 200
        )

        println("ArtNet patch:")
        println(artnetPatch.toString())

        colorModeHSB()
        reloadGenerators(canvasWidth, canvasHeight)
        watchFiles()
    }

    override fun draw() {
        updatePatchBox()
        background(0f, 0f, 10f)
        renderCanvas()
        drawOutput()
        if (Config.OUTPUT_ENABLED) {
            sendData()
        }

        // Debug view in upper left corner
        pushPop {
            image(canvas, 0f, 0f)
        }
    }

    private fun renderCanvas() = canvas.apply {
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        draw {
            clear()
            generators.filter { it.value.enabled }.forEach { (_, gen) ->
                val (graphics, blendMode) = try {
                    gen.generate()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    return@forEach
                }

                blend(
                    graphics,
                    0,
                    0,
                    graphics.width,
                    graphics.height,
                    0,
                    0,
                    width,
                    height,
                    blendMode.id
                )
            }
        }
    }

    private fun drawOutput() {
        pushPop {
            val cellSize = Config.SIZE
            val space = Config.SPACE
            val drawnWidth = canvasWidth * cellSize + canvasWidth * space
            val drawnHeight = canvasHeight * cellSize + canvasHeight * space

            translateCenter()
            rectMode(PConstants.CORNER)
            noStroke()
            canvas.loadPixels()

            for (x in 0 until canvasWidth) {
                for (y in 0 until canvasHeight) {
                    fill(canvas.pixelAt(x, y))
                    square(
                        (x.toFloat() * cellSize + space * x) - drawnWidth / 2f,
                        (y.toFloat() * cellSize + space * y) - drawnHeight / 2f,
                        cellSize
                    )
                }
            }
        }
    }

    private fun sendData() = with(artnetPatch) {
        scan(canvas)
        universeData.forEach { (universe, byteArray) ->
            artnetClient.unicastDmx(Config.NODE_IP, 0, universe, byteArray)
        }
    }

    /**
     * Sketch data path override. It's wrong when using local Processing installation core jars.
     * Sketch folder path cannot be passed as an argument, does not play well with DI.
     */
    override fun dataPath(where: String): String {
        return System.getProperty("user.dir") + "/data/" + where
    }

    /**
     * Sketch data path override. It's wrong when using local Processing installation core jars.
     * Sketch folder path cannot be passed as an argument, does not play well with DI.
     */
    override fun dataFile(where: String): File {
        return File(System.getProperty("user.dir") + "/data/" + where)
    }
}