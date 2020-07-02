package dev.matsem.ala

import ch.bildspur.artnet.ArtNetClient
import ddf.minim.Minim
import ddf.minim.ugens.Sink
import dev.matsem.ala.tools.dmx.ArtnetPatch
import dev.matsem.ala.tools.extensions.*
import dev.matsem.ala.tools.kontrol.KontrolF1
import dev.matsem.ala.tools.live.FileWatcher
import dev.matsem.ala.tools.live.GeneratorLiveScript
import dev.matsem.ala.tools.live.PatchBox
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File

class MainSketch : PApplet() {

    object Config {
        const val LED_WIDTH = 150
        const val LED_HEIGHT = 5
        const val SPACE = 2
        const val SIZE = 5f
        const val NODE_IP = "192.168.1.18"
        const val OUTPUT_ENABLED = false
        const val GENERATORS_DIR = "src/main/kotlin/dev/matsem/ala/generators"
    }

    private var canvasWidth = Config.LED_WIDTH
    private var canvasHeight = Config.LED_HEIGHT

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
        for (y in 0 until patchHeight) {
            patch(0 until patchWidth, y until y + 1, ArtnetPatch.Direction.SNAKE_NE, y, 0)
        }
    }
    // endregion

    // region Core stuff
    private lateinit var mainCanvas: PGraphics
    private val lock = Any()
    private val gens = mutableMapOf<File, GeneratorLiveScript>()
    private val fileWatcher = FileWatcher()
    // endregion

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
        loadGenerators(canvasWidth, canvasHeight)
        watchFiles()
    }

    override fun draw() {
        updatePatchBox()
        background(0f, 0f, 10f)

        gens.forEach { (_, gen) -> gen.update() }

        renderMainCanvas()
        drawLayerPreviews()

        drawMainPreview()

        if (Config.OUTPUT_ENABLED) {
            sendData()
        }
    }

    private fun renderMainCanvas() = mainCanvas.draw {
        clear()
        colorModeHSB()
        gens
            .values
            .filter { it.isEnabled }
            .mapNotNull { it.result }
            .forEach {
                blend(it.graphics, 0, 0, it.graphics.width, it.graphics.height, 0, 0, width, height, it.blendMode.id)
            }
    }

    private fun drawLayerPreviews() {
        pushPop {
            scale(2f)
            gens
                .values
                .filter { it.isEnabled }
                .mapNotNull { it.result }
                .forEachIndexed { i, layer ->
                    pushPop {
                        translate(16f, 16f + (i * 8f))
                        image(layer.graphics, 0f, 0f)
                    }
                }
        }
    }

    private fun drawMainPreview() {
        pushPop {
            val cellSize = Config.SIZE
            val space = Config.SPACE
            val drawnWidth = canvasWidth * cellSize + canvasWidth * space
            val drawnHeight = canvasHeight * cellSize + canvasHeight * space

            translateCenter()
            rectMode(PConstants.CORNER)
            noStroke()
            mainCanvas.loadPixels()

            for (x in 0 until canvasWidth) {
                for (y in 0 until canvasHeight) {
                    fill(mainCanvas.pixelAt(x, y))
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
        scan(mainCanvas)
        universeData.forEach { (universe, byteArray) ->
            artnetClient.unicastDmx(Config.NODE_IP, 0, universe, byteArray)
        }
    }

    private fun loadGenerators(w: Int, h: Int) {
        mainCanvas = createGraphics(w, h, PConstants.P2D)

        println("Loading generators @ ${w}x$h px")
        val liveScripts = File(Config.GENERATORS_DIR)
            .listFiles { file: File -> file.name.endsWith("kts") }
            ?.map { it.absoluteFile }
            ?: return

        if (liveScripts.isEmpty()) {
            println("No available files")
            return
        }

        unloadAllScripts()
        liveScripts.forEach { file ->
            loadScript(file, w, h)
        }
    }

    private fun watchFiles() {
        fileWatcher.watchPath(
            File(Config.GENERATORS_DIR).toPath(),
            onCreate = {
                println("🔥 New script available: ${it.name}")
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

    private fun loadScript(scriptFile: File, w: Int, h: Int) {
        synchronized(lock) {
            gens
                .getOrPut(scriptFile) {
                    GeneratorLiveScript(scriptFile, this, sink, lineIn, patchBox, w, h)
                }
                .reload()
        }
    }

    private fun unloadScript(scriptFile: File) {
        synchronized(lock) {
            gens[scriptFile]?.unload()
            gens.remove(scriptFile)
        }
        println("🗑 Script ${scriptFile.name} unloaded")
    }

    private fun unloadAllScripts() {
        synchronized(lock) {
            gens.forEach { (_, gen) ->
                gen.unload()
            }
            gens.clear()
        }
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