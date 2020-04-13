package dev.matsem.ala

import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PVector
import processing.event.MouseEvent
import java.io.File
import kotlin.random.Random

class Patcher(private val sketch: PApplet) {

    private val patchBoxes = mutableListOf<PatchBox>()
    private var focusedView: PatchBox? = null

    init {
        sketch.registerMethod("mouseEvent", this)
        sketch.registerMethod("draw", this)
    }

    fun createPatchBox(posX: Float, posY: Float): PatchBox {
        return PatchBox(
            sketch,
            posX,
            posY,
            "${patchBoxes.size}"
        ).also { patchBoxes += it }
    }

    fun mouseEvent(event: MouseEvent) {
        val stillFocused = focusedView?.onMouseEvent(event) ?: false
        if (!stillFocused) {
            focusedView = null

            loop@ for (box in patchBoxes) {
                if (box.onMouseEvent(event)) {
                    focusedView = box
                    println("focused view changed to: ${box.text}")
                    break@loop
                }
            }

            focusedView?.let { focusedView ->
                println("changing focus order")
                patchBoxes.sortBy { if (it == focusedView) 1 else 0 }
                println("new focus order: ${patchBoxes.joinToString { it.text }}")
            }
        }
    }

    fun draw() {
        sketch.hint(PConstants.DISABLE_DEPTH_TEST)
        patchBoxes.forEach { it.draw() }
        sketch.hint(PConstants.ENABLE_DEPTH_TEST)
    }
}

class PatchBox(
    private val sketch: PApplet,
    private var posX: Float = 0f,
    private var posY: Float = 0f,
    val text: String
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
    private val bgColor = sketch.color(Random.nextFloat() * 360f, 50f, 50f)

    private var isMouseOver: Boolean = false
    private var dragState = DragState.IDLE
    private var dragAnchor = PVector(0f, 0f)

    val pg: PGraphics = sketch.createGraphics(width, height, PConstants.P2D)

    private fun updateState() {
        constrainPosition()
    }

    fun mouseInViewBounds() = sketch.mouseX.toFloat() in (posX..posX + width)
            && sketch.mouseY.toFloat() in (posY..posY + height)

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
                fill(bgColor)
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
                text("$text", 0f, 0f)
            }
        }
    }

    fun draw() {
        updateState()
        drawPg()
        sketch.pushPop {
            translate(posX, posY)
            image(pg, 0f, 0f)
        }
    }

    /**
     * Returns [true] if event has been consumed.
     */
    fun onMouseEvent(event: MouseEvent): Boolean {
        val consumed = when {
            event.action == MouseEvent.DRAG && dragState == DragState.DRAGGING -> {
                posX = sketch.mouseX - dragAnchor.x
                posY = sketch.mouseY - dragAnchor.y
                true
            }
            event.action == MouseEvent.PRESS && isMouseOver -> {
                dragState = DragState.DRAGGING
                dragAnchor = PVector(sketch.mouseX - posX, sketch.mouseY - posY)
                true
            }
            event.action == MouseEvent.RELEASE && dragState == DragState.DRAGGING -> {
                dragState = DragState.IDLE
                true
            }
            else -> false
        }

        isMouseOver = mouseInViewBounds()
        return consumed
    }
}

class ExperimentSketch : PApplet() {

    lateinit var patcher: Patcher

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setResizable(true)
        colorModeHSB()

        patcher = Patcher(this)
        repeat(5) {
            patcher.createPatchBox(
                Random.nextFloat() * width,
                Random.nextFloat() * height
            )
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