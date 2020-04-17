package dev.matsem.ala.ui.patch

import dev.matsem.ala.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import processing.event.MouseEvent

class Patcher(private val sketch: PApplet) {

    private val cursor = Cursor(sketch.mouseX.toFloat(), sketch.mouseY.toFloat())
    private val patchBoxes = mutableListOf<PatchBox>()
    private var focusedView: PatchBox? = null

    private var isPanning = false
    private var panX = 0f
    private var panY = 0f
    private val font: PFont = sketch.createFont("fonts/pixelmix/pixelmix.ttf", 12f, false)

    init {
        sketch.registerMethod("mouseEvent", this)
        sketch.registerMethod("draw", this)
    }

    fun createPatchBox(posX: Float, posY: Float, inputs: List<String>, outputs: List<String>): PatchBox {
        return PatchBox(
            sketch,
            cursor,
            font,
            posX,
            posY,
            inputs,
            outputs
        ).also { patchBoxes += it }
    }

    fun mouseEvent(event: MouseEvent) {
        cursor.x = sketch.mouseX - panX
        cursor.y = sketch.mouseY - panY

        when {
            event.action == MouseEvent.PRESS && event.button == PConstants.CENTER -> isPanning = true
            event.action == MouseEvent.RELEASE && event.button == PConstants.CENTER -> isPanning = false
        }

        if (isPanning && event.action == MouseEvent.DRAG) {
            panX += sketch.mouseX - sketch.pmouseX
            panY += sketch.mouseY - sketch.pmouseY
        }

        val stillFocused = focusedView?.onMouseEvent(event) ?: false
        if (!stillFocused) {
            focusedView = null

            loop@ for (box in patchBoxes) {
                if (box.onMouseEvent(event)) {
                    focusedView = box
                    break@loop
                }
            }

            focusedView?.let { focusedView ->
                patchBoxes.sortBy { if (it == focusedView) 1 else 0 }
            }
        }
    }

    fun draw() {
        sketch.hint(PConstants.DISABLE_DEPTH_TEST)
        sketch.pushPop {
            translate(panX, panY)
            patchBoxes.forEach { it.draw() }
        }
        sketch.hint(PConstants.ENABLE_DEPTH_TEST)
    }
}