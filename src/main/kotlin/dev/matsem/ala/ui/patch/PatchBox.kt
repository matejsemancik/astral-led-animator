package dev.matsem.ala.ui.patch

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PVector
import processing.event.MouseEvent
import kotlin.math.max

class PatchBox(
    private val sketch: PApplet,
    private val cursor: Cursor,
    private val patchStyle: PatchStyle,
    private var posX: Float = 0f,
    private var posY: Float = 0f,
    private val inputs: List<String>,
    private val outputs: List<String>
) : PConstants {

    enum class DragState {
        DRAGGING, IDLE
    }

    // region Drawing
    private var pg: PGraphics = sketch.createGraphics(calculateWidth(), calculateHeight(), PConstants.P2D)
    // endregion

    // region State
    private var isMouseOver: Boolean = false
    private var dragState = DragState.IDLE
    private var dragAnchor = PVector(0f, 0f)
    // endregion

    // region Core
    private val inputPorts = inputs.map { name -> InputPort(name, patchStyle.idleColor) }
    private val outputPorts = outputs.map { name -> OutputPort(name, patchStyle.idleColor) }
    // endregion

    private fun drawPg() = with(pg) {
        draw {
            clear()
            colorModeHSB()
            // Background
            pushPop {
                background(patchStyle.bgColor)
            }
            // draw inputs
            pushPop {
                textAlign(PConstants.LEFT, PConstants.TOP)
                textFont(patchStyle.font)
                translate(0f, patchStyle.paddingTop.toFloat())
                val fontHeight = patchStyle.font.size.toFloat()
                val portSize = patchStyle.portSize.toFloat()
                inputPorts.forEachIndexed { i, port ->
                    val margin = if (i == 0) 0f else patchStyle.textMargin.toFloat()
                    fill(getColor())
                    text(port.name, portSize + 2, i * fontHeight + margin * i)
                    fill(port.color)
                    rect(0f, i * fontHeight + margin * i, portSize, portSize)
                }
            }

            // draw outputs
            pushPop {
                textAlign(PConstants.RIGHT, PConstants.TOP)
                textFont(patchStyle.font)
                translate(pg.width.toFloat(), patchStyle.paddingTop.toFloat())
                val fontHeight = patchStyle.font.size.toFloat()
                val portSize = patchStyle.portSize.toFloat()
                outputPorts.forEachIndexed { i, port ->
                    val margin = if (i == 0) 0f else patchStyle.textMargin.toFloat()
                    fill(getColor())
                    text(port.name, 0f - portSize - 2, i * fontHeight + margin * i)
                    fill(port.color)
                    rect(0f - portSize, i * fontHeight + margin * i, portSize, portSize)
                }
            }
        }
    }

    internal fun draw() {
        drawPg()
        sketch.pushPop {
            translate(posX, posY)
            image(pg, 0f, 0f)
        }
    }

    /**
     * Returns [true] if event has been consumed.
     */
    internal fun onMouseEvent(event: MouseEvent): Boolean {
        isMouseOver = mouseInViewBounds()
        return when {
            event.action == MouseEvent.DRAG && dragState == DragState.DRAGGING -> {
                posX = cursor.x - dragAnchor.x
                posY = cursor.y - dragAnchor.y
                true
            }
            event.action == MouseEvent.PRESS && isMouseOver && event.button == PConstants.LEFT -> {
                dragState = DragState.DRAGGING
                dragAnchor = PVector(cursor.x - posX, cursor.y - posY)
                true
            }
            event.action == MouseEvent.RELEASE && dragState == DragState.DRAGGING -> {
                dragState = DragState.IDLE
                true
            }
            else -> false
        }
    }

    private fun mouseInViewBounds() = cursor.x in (posX..posX + pg.width)
            && cursor.y in (posY..posY + pg.height)

    private fun getColor() = if (isMouseOver || dragState == DragState.DRAGGING) {
        patchStyle.activeColor
    } else {
        patchStyle.idleColor
    }

    private fun calculateHeight(): Int {
        return patchStyle.paddingTop + patchStyle.paddingBottom + max(
            inputs.count() * (patchStyle.font.size + patchStyle.textMargin),
            outputs.count() * (patchStyle.font.size + patchStyle.textMargin)
        )
    }

    private fun calculateWidth(): Int {
        sketch.textFont(patchStyle.font)
        val inputsWidth = inputs.maxBy { it.count() }?.let { sketch.textWidth(it).toInt() } ?: 0
        val outputsWidth = outputs.maxBy { it.count() }?.let { sketch.textWidth(it).toInt() } ?: 0
        return inputsWidth + outputsWidth + patchStyle.portSize + 20 // Some space between
    }
}