package dev.matsem.ala

import ch.bildspur.artnet.ArtNetClient
import ddf.minim.Minim
import ddf.minim.ugens.Constant
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.*
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
        const val LED_WIDTH = 150
        const val LED_HEIGHT = 6
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
    private lateinit var knight1: KnightRiderGenerator
    private lateinit var knight2: KnightRiderGenerator
    private lateinit var strobe1: StrobeGenerator
    private lateinit var laser1: LaserGenerator
    private lateinit var beatDetect1: BeatDetectGenerator
    private lateinit var fft1: FFTGenerator
    // endregion

    private fun createObjects(w: Int, h: Int) {
        canvas = createGraphics(w, h, PConstants.P2D)
        if (::knight1.isInitialized) knight1.unpatch()
        knight1 = KnightRiderGenerator(this, w, h, sink)
        if (::knight2.isInitialized) knight2.unpatch()
        knight2 = KnightRiderGenerator(this, w, h, sink)
        if (::strobe1.isInitialized) strobe1.unpatch()
        strobe1 = StrobeGenerator(this, w, h, sink)
        if (::laser1.isInitialized) laser1.unpatch()
        laser1 = LaserGenerator(this, w, h, sink)
        if (::beatDetect1.isInitialized) beatDetect1.unpatch()
        beatDetect1 = BeatDetectGenerator(this, w, h, lineIn)
        if (::fft1.isInitialized) fft1.unpatch()
        fft1 = FFTGenerator(this, w, h, lineIn, sink)

        slider1.patch(fft1.hue)
        slider2.patch(fft1.fading)
        slider3.patch(fft1.widthSpan)
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
            val f = fft1.generate()
            blend(f, 0, 0, f.width, f.height, 0, 0, width, height, PConstants.ADD)
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