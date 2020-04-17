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
    private var selectedInput: String? = null
    private var selectedOutput: String? = null
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
                inputs.forEachIndexed { i, input ->
                    val margin = if (i == 0) 0f else patchStyle.textMargin.toFloat()
                    fill(if (input == selectedInput) patchStyle.activeColor else patchStyle.idleColor)
                    rect(0f, i * fontHeight + margin * i, portSize, portSize)
                    fill(getColor())
                    text(input, portSize + 2, i * fontHeight + margin * i)
                }
            }

            // draw outputs
            pushPop {
                textAlign(PConstants.RIGHT, PConstants.TOP)
                textFont(patchStyle.font)
                translate(pg.width.toFloat(), patchStyle.paddingTop.toFloat())
                val fontHeight = patchStyle.font.size.toFloat()
                val portSize = patchStyle.portSize.toFloat()
                outputs.forEachIndexed { i, output ->
                    val margin = if (i == 0) 0f else patchStyle.textMargin.toFloat()
                    fill(if (output == selectedOutput) patchStyle.activeColor else patchStyle.idleColor)
                    rect(0f - portSize, i * fontHeight + margin * i, portSize, portSize)
                    fill(getColor())
                    text(output, 0f - portSize - 2, i * fontHeight + margin * i)
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
        if (isMouseOver) {
            selectedInput = findSelectedInput()
            selectedOutput = findSelectedOutput()
        }
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

    private fun findSelectedInput(): String? {
        val paddingTop = patchStyle.paddingTop
        inputs.forEachIndexed { i, input ->
            val margin = if (i == 0) 0f else patchStyle.textMargin.toFloat()
            val portX = 0f
            val portY = i * patchStyle.font.size + margin * i + paddingTop
            val portSize = patchStyle.portSize.toFloat()
            if (cursor.relative().x in portX..(portX + portSize) && cursor.relative().y in portY..(portY + portSize)) {
                return input
            }
        }
        return null
    }

    private fun findSelectedOutput(): String? {
        val paddingTop = patchStyle.paddingTop
        outputs.forEachIndexed { i, output ->
            val margin = if (i == 0) 0f else patchStyle.textMargin.toFloat()
            val portX = pg.width.toFloat() - patchStyle.portSize
            val portY = i * patchStyle.font.size + margin * i + paddingTop
            val portSize = patchStyle.portSize.toFloat()
            if (cursor.relative().x in portX..(portX + portSize) && cursor.relative().y in portY..(portY + portSize)) {
                return output
            }
        }
        return null
    }

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

    private fun Cursor.relative() = this.copy(x = x - posX, y = y - posY)
}