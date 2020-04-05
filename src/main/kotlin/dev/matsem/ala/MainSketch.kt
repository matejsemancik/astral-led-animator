package dev.matsem.ala

import dev.matsem.ala.tools.extensions.*
import dev.matsem.ala.tools.kontrol.KontrolF1
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.properties.Delegates

class MainSketch : PApplet() {

    private val kontrol = KontrolF1()
    private var a = 0f
    private var b = 0f
    private var c = 0f
    private var d = 0f

    private var canvasWidth: Int by Delegates.vetoable(initialValue = 30) { _, oldVal, newVal ->
        val hasChanged = oldVal != newVal
        if (hasChanged) {
            canvas = createGraphics(newVal, canvasHeight, PConstants.P3D)
        }
        hasChanged
    }
    private var canvasHeight: Int by Delegates.vetoable(initialValue = 5) { _, oldVal, newVal ->
        val hasChanged = oldVal != newVal
        if (hasChanged) {
            canvas = createGraphics(canvasWidth, newVal, PConstants.P3D)
        }
        hasChanged
    }

    private lateinit var canvas: PGraphics

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setTitle("Astral LED Animator")
        surface.setResizable(true)
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        kontrol.connect()
        canvas = createGraphics(canvasWidth, canvasHeight, PConstants.P3D)
    }

    override fun draw() {
        a = kontrol.knob1.midiRange(1f)
        b = kontrol.knob2.midiRange(1f)
        c = kontrol.knob3.midiRange(1f)
        d = kontrol.knob4.midiRange(1f)

        canvasWidth = (a * 150).toInt().constrain(low = 1)
        canvasHeight = (b * 150f).toInt().constrain(low = 1)

        background(0f, 0f, 10f)

        renderCanvas()
        drawOutput()

        pushPop {
            image(canvas, 0f, 0f)
        }
    }

    private fun renderCanvas() = canvas.apply {
        draw {
            colorMode(PConstants.HSB, 360f, 100f, 100f)
            stroke(0)
            strokeWeight(2f)
            fill(160f, 100f, 100f)
            clear()
            background(0)
            pushPop {
                translateCenter()
                rotateX(angularTimeS(10f))
                rotateY(angularTimeS(20f))
                rotateZ(angularTimeS(30f))
                box(shorterDimension() / 2f)
            }
        }
    }

    private fun drawOutput() {
        pushPop {
            val cellSize = (c * 10f)
            val space = (d * 5f).toInt()
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
}