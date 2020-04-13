package dev.matsem.ala

import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PVector
import processing.event.MouseEvent
import java.io.File
import kotlin.random.Random

class PatchBox(
    private val sketch: PApplet,
    private var posX: Float = 0f,
    private var posY: Float = 0f,
    private var z: Int
) : PConstants {

    enum class DragState {
        DRAGGING, IDLE
    }

    var width = 100
    var height = 50
    private val strokeW = 2f
    private val cornerRadius = 4f
    private val idleColor = sketch.color(0f, 0f, 80f)
    private val mouseOverColor = sketch.color(0f, 0f, 100f)

    private var isMouseOver: Boolean = false
    private var dragState = DragState.IDLE
    private var dragAnchor = PVector(0f, 0f)

    val pg: PGraphics = sketch.createGraphics(width, height, PConstants.P2D)

    init {
        sketch.registerMethod("draw", this)
        sketch.registerMethod("mouseEvent", this)
    }

    private fun updateState() {
        isMouseOver =
            sketch.mouseX.toFloat() in (posX..posX + width) && sketch.mouseY.toFloat() in (posY..posY + height)

        if (dragState == DragState.DRAGGING) {
            posX = sketch.mouseX - dragAnchor.x
            posY = sketch.mouseY - dragAnchor.y
        }
    }

    private fun constrainPosition() = with(sketch) {
        posX = posX.constrain(low = 0f, high = width.toFloat() - 10f)
        posY = posY.constrain(low = 0f, high = height.toFloat() - 10f)
    }

    private fun getColor() = if (isMouseOver || dragState == DragState.DRAGGING) {
        mouseOverColor
    } else {
        idleColor
    }

    private fun drawPg() = with(pg) {
        draw {
            clear()
            colorModeHSB()
            pushPop {
                rectMode(PConstants.CORNER)
                noFill()
                strokeWeight(strokeW)
                stroke(getColor())
                rect(
                    0f + strokeW,
                    0f + strokeW,
                    width.toFloat() - strokeW * 2,
                    height.toFloat() - strokeW * 2,
                    cornerRadius,
                    cornerRadius,
                    cornerRadius,
                    cornerRadius
                )
            }
            pushPop {
                noStroke()
                fill(getColor())
                textAlign(PConstants.CENTER, PConstants.CENTER)
                textSize(20f)
                translateCenter()
                text("$z", 0f, 0f)
            }
        }
    }

    fun draw() {
        updateState()
        drawPg()
        sketch.pushPop {
            hint(PConstants.DISABLE_DEPTH_TEST)
            translate(posX, posY)
            image(pg, 0f, 0f)
            hint(PConstants.ENABLE_DEPTH_TEST)
        }
    }

    fun mouseEvent(event: MouseEvent) {
        when {
            event.action == MouseEvent.PRESS && isMouseOver -> {
                dragState = DragState.DRAGGING
                dragAnchor = PVector(sketch.mouseX - posX, sketch.mouseY - posY)
            }
            event.action == MouseEvent.RELEASE && dragState == DragState.DRAGGING -> {
                dragState = DragState.IDLE
                constrainPosition()
            }
        }
    }
}

class ExperimentSketch : PApplet() {

    val patchBoxes = mutableListOf<PatchBox>()

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorModeHSB()
        patchBoxes.apply {
            repeat(5) {
                patchBoxes += PatchBox(
                    this@ExperimentSketch,
                    Random.nextFloat() * width,
                    Random.nextFloat() * height,
                    it
                )
            }
        }
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