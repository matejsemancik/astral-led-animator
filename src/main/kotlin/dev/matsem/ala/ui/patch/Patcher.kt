package dev.matsem.ala.ui.patch

import dev.matsem.ala.tools.extensions.isDrag
import dev.matsem.ala.tools.extensions.isMiddlePress
import dev.matsem.ala.tools.extensions.isRelease
import dev.matsem.ala.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import processing.event.MouseEvent

data class PatchCord(val fromBox: PatchBox, val fromPort: PatchPort, val toBox: PatchBox, val toPort: PatchPort)

class Patcher(private val sketch: PApplet) {

    private val cursor = Cursor(sketch.mouseX.toFloat(), sketch.mouseY.toFloat())
    private val patchBoxes = mutableListOf<PatchBox>()
    private var focusedView: PatchBox? = null

    private var isPanning = false
    private var panX = 0f
    private var panY = 0f
    private val font: PFont = sketch.createFont("fonts/pixelmix/pixelmix.ttf", 12f, false)

    private var draggingFromPort: Pair<PatchBox, PatchPort>? = null
    private var draggingToPort: Pair<PatchBox, PatchPort>? = null
    private val patchCords = mutableListOf<PatchCord>()

    init {
        sketch.registerMethod("mouseEvent", this)
        sketch.registerMethod("draw", this)
    }

    fun createPatchBox(posX: Float, posY: Float, inputs: List<String>, outputs: List<String>): PatchBox {
        return PatchBox(
            sketch,
            this,
            cursor,
            font,
            posX,
            posY,
            inputs,
            outputs
        ).also { patchBoxes += it }
    }

    internal fun onConnectionStartDrag(patchBox: PatchBox, port: PatchPort) {
        draggingFromPort = patchBox to port
    }

    fun mouseEvent(event: MouseEvent) {
        cursor.x = sketch.mouseX - panX
        cursor.y = sketch.mouseY - panY

        when {
            event.isMiddlePress() -> isPanning = true
            event.isDrag() && draggingFromPort != null -> {
                draggingFromPort?.let { (sourceBox, sourcePort) ->
                    draggingToPort = patchBoxes
                        .find { it.findMouseTargetFor(sourcePort) != null }
                        ?.let { it to it.findMouseTargetFor(sourcePort)!! }
                }
                return
            }
            event.isRelease() -> {
                isPanning = false
                if (draggingFromPort != null && draggingToPort != null) {
                    patchCords += PatchCord(
                        draggingFromPort!!.first, draggingFromPort!!.second,
                        draggingToPort!!.first, draggingToPort!!.second
                    )
                }
                draggingToPort = null
                draggingFromPort = null
            }
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

            draggingFromPort?.let { (sourceBox, sourcePort) ->
                val fromX = sourceBox.posX + sourcePort.x + sourcePort.size / 2f
                val fromY = sourceBox.posY + sourcePort.y + sourcePort.size / 2f
                val toX = cursor.x
                val toY = cursor.y
                noFill()
                strokeWeight(2f)
                stroke(color(0f, 0f, 100f))
                line(fromX, fromY, toX, toY)
            }

            patchCords.forEach {
                noFill()
                strokeWeight(2f)
                stroke(color(0f, 0f, 100f))
                line(
                    it.fromBox.posX + it.fromPort.x + it.fromPort.size / 2f,
                    it.fromBox.posY + it.fromPort.y + it.fromPort.size / 2f,
                    it.toBox.posX + it.toPort.x + it.toPort.size / 2f,
                    it.toBox.posY + it.toPort.y + it.toPort.size / 2f
                )
            }
        }

        sketch.hint(PConstants.ENABLE_DEPTH_TEST)
    }
}