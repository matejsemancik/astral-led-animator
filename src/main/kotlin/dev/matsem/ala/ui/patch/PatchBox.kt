package dev.matsem.ala.ui.patch

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.pushPop
import dev.matsem.ala.tools.extensions.translateCenter
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PVector
import processing.event.MouseEvent
import kotlin.random.Random

class PatchBox(
    private val sketch: PApplet,
    private val cursor: Cursor,
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

    private fun mouseInViewBounds() = cursor.x in (posX..posX + width)
            && cursor.y in (posY..posY + height)

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

        isMouseOver = mouseInViewBounds()
        return consumed
    }
}