package dev.matsem.ala

import dev.matsem.ala.generators.KnightRiderGenerator
import dev.matsem.ala.tools.extensions.*
import dev.matsem.ala.tools.kontrol.KontrolF1
import dev.matsem.ala.tools.patch.ArtnetPatch
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
    }

    private val kontrol = KontrolF1()
    private var a = 0f
    private var b = 0f
    private var c = 0f
    private var d = 0f

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

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setTitle("Astral LED Animator")
        surface.setResizable(true)
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        createObjects(canvasWidth, canvasHeight)
        kontrol.connect()

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
        a = kontrol.knob1.midiRange(1f)
        b = kontrol.knob2.midiRange(1f)
        c = kontrol.knob3.midiRange(1f)
        d = kontrol.knob4.midiRange(1f)

        background(0f, 0f, 10f)
        renderCanvas()
        drawOutput()

        pushPop {
            image(canvas, 0f, 0f)
        }
    }

    private fun renderCanvas() = canvas.apply {
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        draw {
            clear()
            val k2 = knight2.generate(fHz = a * 2f, w = (b * 8).toInt(), color = color(30f, 100f, 100f), fading = c)
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