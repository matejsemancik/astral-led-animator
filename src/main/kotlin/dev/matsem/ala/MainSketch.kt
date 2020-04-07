package dev.matsem.ala

import ch.bildspur.artnet.ArtNetClient
import dev.matsem.ala.generators.KnightRiderGenerator
import dev.matsem.ala.tools.dmx.ArtnetPatch
import dev.matsem.ala.tools.extensions.*
import dev.matsem.ala.tools.kontrol.KontrolF1
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File
import kotlin.properties.Delegates

class MainSketch : PApplet() {

    object Config {
        const val LED_WIDTH = 30
        const val LED_HEIGHT = 5
        const val SPACE = 2
        const val SIZE = 10f
        const val NODE_IP = "192.168.1.18"
    }

    private val kontrol = KontrolF1()
    private var a1 = 0f
    private var b1 = 0f
    private var c1 = 0f
    private var d1 = 0f
    private var a2 = 0f
    private var b2 = 0f
    private var c2 = 0f
    private var d2 = 0f

    private var canvasWidth: Int by Delegates.vetoable(initialValue = Config.LED_WIDTH) { _, oldVal, newVal ->
        val hasChanged = oldVal != newVal
        if (hasChanged) {
            createObjects(newVal, canvasHeight)
        }
        hasChanged
    }
    private var canvasHeight: Int by Delegates.vetoable(initialValue = Config.LED_HEIGHT) { _, oldVal, newVal ->
        val hasChanged = oldVal != newVal
        if (hasChanged) {
            createObjects(canvasWidth, newVal)
        }
        hasChanged
    }

    private lateinit var canvas: PGraphics
    private lateinit var patch: ArtnetPatch
    private lateinit var knight1: KnightRiderGenerator
    private lateinit var knight2: KnightRiderGenerator
    private lateinit var artnetClient: ArtNetClient

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setTitle("Astral LED Animator")
        surface.setResizable(true)

        kontrol.connect()
        artnetClient = ArtNetClient().apply { start() }

        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)

        createObjects(canvasWidth, canvasHeight)

        patch = ArtnetPatch(Config.LED_WIDTH, Config.LED_HEIGHT).apply {
            patch(0 until Config.LED_WIDTH, 0 until Config.LED_HEIGHT, ArtnetPatch.Direction.SNAKE_NE, 0, 0)
        }
        println(patch.toString())
    }

    private fun createObjects(w: Int, h: Int) {
        canvas = createGraphics(w, h, PConstants.P2D)
        knight1 = KnightRiderGenerator(this, w, h)
        knight2 = KnightRiderGenerator(this, w, h)
    }

    override fun draw() {
        a1 = kontrol.knob1.midiRange(1f)
        b1 = kontrol.knob2.midiRange(1f)
        c1 = kontrol.knob3.midiRange(1f)
        d1 = kontrol.knob4.midiRange(1f)
        a2 = kontrol.slider1.midiRange(1f)
        b2 = kontrol.slider2.midiRange(1f)
        c2 = kontrol.slider3.midiRange(1f)
        d2 = kontrol.slider4.midiRange(1f)

        background(0f, 0f, 10f)
        renderCanvas()
        drawOutput()
        sendData()

        pushPop {
            image(canvas, 0f, 0f)
        }
    }

    private fun renderCanvas() = canvas.apply {
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        draw {
            clear()
            val k1 = knight1.generate(color = color(a1.remap(0f, 1f, 0f, 360f), 100f, 100f), fHz = b1 * 3f, w = (c1 * 8).toInt(), fading = d1)
            val k2 = knight2.generate(color = color(a1.remap(0f, 1f, 0f, 360f), 100f, 100f), fHz = b1 * 3f, w = (c1 * 8).toInt(), fading = d1)
            blend(k1, 0, 0, k1.width, k1.height, 0, 0, width, height, PConstants.ADD)
            blend(k2, 0, 0, k2.width, k2.height, 0, 0, width, height, PConstants.ADD)
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

    private fun sendData() {
        patch.scan(canvas)
        patch.universeData.forEach { (universe, byteArray) ->
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