package dev.matsem.ala

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import java.io.File

class Patchable(private val sketch: PApplet, private var posX: Float = 0f, private var posY: Float = 0f) : PConstants {

    var width = 100
    var height = 50
    val strokeWeight = 1f
    val idleColor = sketch.color(0f, 0f, 70f)
    val mouseOverColor = sketch.color(0f, 0f, 100f)

    val pg: PGraphics = sketch.createGraphics(width, height, PConstants.P2D)

    init {
        sketch.registerMethod("draw", this)
    }

    private fun drawPg() = with(pg) {
        draw {
            clear()
            colorModeHSB()
            pushPop {
                rectMode(PConstants.CORNER)
                noFill()
                strokeWeight(strokeWeight)
                stroke(idleColor)
                rect(0f, 0f, width.toFloat() - strokeWeight, height.toFloat() - strokeWeight)
            }
        }
    }

    fun draw() {
        drawPg()
        sketch.pushPop {
            hint(PConstants.DISABLE_DEPTH_TEST)
            translate(posX, posY)
            image(pg, 0f, 0f)
            hint(PConstants.ENABLE_DEPTH_TEST)
        }
    }
}

class ExperimentSketch : PApplet() {

    val patchables = mutableListOf<Patchable>()

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorModeHSB()
        patchables += Patchable(this, 10f, 10f)
    }

    override fun draw() {
        background(0)
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