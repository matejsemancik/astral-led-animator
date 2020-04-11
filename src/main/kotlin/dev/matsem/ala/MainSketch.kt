package dev.matsem.ala

import ch.bildspur.artnet.ArtNetClient
import ddf.minim.Minim
import ddf.minim.ugens.Sink
import dev.matsem.ala.generators.KnightRiderGenerator
import dev.matsem.ala.generators.LaserGenerator
import dev.matsem.ala.generators.StrobeGenerator
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
    private var a1 = 0f
    private var b1 = 0f
    private var c1 = 0f
    private var d1 = 0f
    private var a2 = 0f
    private var b2 = 0f
    private var c2 = 0f
    private var d2 = 0f
    // endregion

    // region Minim, oscillators, audio stuff
    private val minim = Minim(this)
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
            val g = knight1.generate(
                fHz = a1 * 20f,
                amplitude = b1 * 2f,
                beamWidth = c1.mapp(1f, width.toFloat()).toInt().constrain(low = 1),
                color = color(0f, 0f, 100f),
                fading = d1
            )
            blend(g, 0, 0, g.width, g.height, 0, 0, width, height, PConstants.ADD)
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