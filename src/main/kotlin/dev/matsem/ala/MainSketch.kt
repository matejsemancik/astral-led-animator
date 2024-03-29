package dev.matsem.ala

import ch.bildspur.artnet.ArtNetClient
import ddf.minim.Minim
import ddf.minim.ugens.Constant
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.BaseGenerator
import dev.matsem.ala.generators.GameOfLifeGenerator
import dev.matsem.ala.tools.dmx.ArtnetPatch
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.midiRange
import dev.matsem.ala.tools.extensions.pixelAt
import dev.matsem.ala.tools.extensions.pushPop
import dev.matsem.ala.tools.extensions.translateCenter
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
        const val OUTPUT_ENABLED = false
    }

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

    // region Kontrol F1 MIDI controller stuff
    private val kontrol = KontrolF1().apply { connect() }
    private var knob1 = Constant(0f)
    private var knob2 = Constant(0f)
    private var knob3 = Constant(0f)
    private var knob4 = Constant(0f)
    private var slider1 = Constant(0f)
    private var slider2 = Constant(0f)
    private var slider3 = Constant(0f)
    private var slider4 = Constant(0f)
    // endregion

    // region Minim, oscillators, audio stuff
    private val minim = Minim(this)
    private val lineIn = minim.lineIn
    private val lineOut = minim.lineOut
    private val sink = Sink().apply { patch(lineOut) }
    // endregion

    // region Art-Net stuff
    private val artnetClient = ArtNetClient().apply { start() }
    private val artnetPatch = ArtnetPatch(Config.LED_WIDTH, Config.LED_HEIGHT).apply {
        patch(0 until patchWidth, 0 until patchHeight, ArtnetPatch.Direction.SNAKE_NE, 0, 0)
    }
    // endregion

    // region Core stuff
    private lateinit var canvas: PGraphics
    private val generators = mutableListOf<BaseGenerator>()
    // endregion

    private fun createObjects(w: Int, h: Int) {
        generators.forEach { it.unpatch() }
        generators.clear()
        canvas = createGraphics(w, h, PConstants.P2D)
//        generators += KnightRiderGenerator(this, sink, w, h)
//        laser1 = LaserGenerator(this, sink, w, h).also { generators += it }
//        beatDetect1 = BeatDetectGenerator(this, sink, w, h, lineIn).also { generators += it }

//        generators += FFTGenerator(this, sink, w, h, lineIn).also {
//            slider1.patch(Multiplier(360f)).patch(it.hue)
//            slider2.patch(it.fading)
//            slider3.patch(it.widthSpan)
//            slider4.patch(it.mirroring)
//        }
//        generators += StrobeGenerator(this, sink, w, h).also {
//            knob4.patch(Multiplier(10f)).patch(it.frequency)
//        }
        generators += GameOfLifeGenerator(this, sink, w, h, lineIn).also {
            knob1.patch(it.hue)
            knob2.patch(it.coolingFactor)
            knob3.patch(it.speed)
            knob4.patch(it.randomizeThrehold)
        }
    }

    private fun kontrolToUgens() {
        knob1.setConstant(kontrol.knob1.midiRange(1f))
        knob2.setConstant(kontrol.knob2.midiRange(1f))
        knob3.setConstant(kontrol.knob3.midiRange(1f))
        knob4.setConstant(kontrol.knob4.midiRange(1f))
        slider1.setConstant(kontrol.slider1.midiRange(1f))
        slider2.setConstant(kontrol.slider2.midiRange(1f))
        slider3.setConstant(kontrol.slider3.midiRange(1f))
        slider4.setConstant(kontrol.slider4.midiRange(1f))
    }

    override fun settings() = size(1280, 720, PConstants.P3D)

    override fun setup() {
        surface.setTitle("Astral LED Animator")
        surface.setResizable(true)
        surface.setSize(
            (canvasWidth * Config.SIZE + canvasWidth * Config.SPACE).toInt() + 200,
            (canvasHeight * Config.SIZE + canvasHeight * Config.SPACE).toInt() + 200
        )

        colorModeHSB()
        createObjects(canvasWidth, canvasHeight)
        println(artnetPatch.toString())
    }

    override fun draw() {
        kontrolToUgens()
        background(0f, 0f, 10f)
        renderCanvas()
        drawOutput()
        if (Config.OUTPUT_ENABLED) {
            sendData()
        }

        pushPop {
            image(canvas, 0f, 0f)
        }
    }

    private fun renderCanvas() = canvas.apply {
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        draw {
            clear()
            generators.forEach {
                val (graphics, blendMode) = it.generate()
                blend(graphics, 0, 0, graphics.width, graphics.height, 0, 0, width, height, blendMode.id)
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