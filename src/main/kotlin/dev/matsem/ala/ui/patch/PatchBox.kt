package dev.matsem.ala.ui.patch

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.isDrag
import dev.matsem.ala.tools.extensions.isLeftPress
import dev.matsem.ala.tools.extensions.isRelease
import dev.matsem.ala.tools.extensions.pushPop
import dev.matsem.ala.tools.extensions.withAlpha
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import processing.core.PGraphics
import processing.core.PVector
import processing.event.MouseEvent
import kotlin.math.max

class PatchBox(
    private val sketch: PApplet,
    private val patcher: Patcher,
    private val cursor: Cursor,
    private val font: PFont,
    var posX: Float = 0f,
    var posY: Float = 0f,
    private val inputs: List<String>,
    private val outputs: List<String>
) : PConstants {

    enum class DragState {
        DRAGGING, IDLE
    }

    // region Drawing
    private val paddingTop: Int = 10
    private val paddingBottom: Int = 10
    private val textMargin: Int = 4
    private val portSize: Int = 8
    private val bgColor: Int = 0x181818.withAlpha()
    private val idleColor: Int = 0xe0e0e0.withAlpha()
    private val activeColor: Int = 0xffffff.withAlpha()

    private var pg: PGraphics = sketch.createGraphics(calculateWidth(), calculateHeight(), PConstants.P2D)
    // endregion

    // region State
    private var isMouseOver: Boolean = false
    private var dragState = DragState.IDLE
    private var dragAnchor = PVector(0f, 0f)
    private var selectedInput: InputPort? = null
    private var selectedOutput: OutputPort? = null
    // endregion

    // region Core
    private val inputPorts = inputs.mapIndexed { i, name ->
        val margin = if (i == 0) 0f else textMargin.toFloat()
        val portX = 0f
        val portY = i * font.size + margin * i + paddingTop
        InputPort(name, portX, portY, portSize.toFloat())
    }

    private val outputPorts = outputs.mapIndexed { i, name ->
        val margin = if (i == 0) 0f else textMargin.toFloat()
        val portX = pg.width.toFloat() - portSize
        val portY = i * font.size + margin * i + paddingTop
        OutputPort(name, portX, portY, portSize.toFloat())
    }
    // endregion

    fun findMouseTargetFor(sourcePort: PatchPort): PatchPort? {
        return findMouseOverPort()?.let { possibleTarget ->
            when {
                sourcePort is OutputPort && possibleTarget is OutputPort -> null
                sourcePort is InputPort && possibleTarget is InputPort -> null
                else -> possibleTarget
            }
        }
    }

    private fun drawPg() = with(pg) {
        draw {
            clear()
            colorModeHSB()
            // Background
            pushPop {
                background(bgColor)
            }
            // draw inputs
            pushPop {
                textAlign(PConstants.LEFT, PConstants.TOP)
                textFont(font)
                inputPorts.forEach {
                    fill(getColor())
                    rect(it.x, it.y, it.size, it.size)
                    fill(getColor())
                    text(it.name, it.x + it.size + 2, it.y)
                }
            }

            // draw outputs
            pushPop {
                textAlign(PConstants.RIGHT, PConstants.TOP)
                textFont(font)
                outputPorts.forEach {
                    fill(getColor())
                    rect(it.x, it.y, it.size, it.size)
                    fill(getColor())
                    text(it.name, it.x - 2, it.y)
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
        selectedInput = findMouseOverPort() as? InputPort
        selectedOutput = findMouseOverPort() as? OutputPort
        return when {
            event.isLeftPress() && selectedOutput != null -> {
                patcher.onConnectionStartDrag(this, selectedOutput!!)
                true
            }
            event.isLeftPress() && selectedInput != null -> {
                patcher.onConnectionStartDrag(this, selectedInput!!)
                true
            }
            event.isDrag() && dragState == DragState.DRAGGING -> {
                posX = cursor.x - dragAnchor.x
                posY = cursor.y - dragAnchor.y
                true
            }
            event.isLeftPress() && isMouseOver -> {
                dragState = DragState.DRAGGING
                dragAnchor = PVector(cursor.x - posX, cursor.y - posY)
                true
            }
            event.isRelease() && dragState == DragState.DRAGGING -> {
                dragState = DragState.IDLE
                true
            }
            else -> false
        }
    }

    private fun mouseInViewBounds() = cursor.x in (posX..posX + pg.width)
            && cursor.y in (posY..posY + pg.height)

    private fun findMouseOverPort(): PatchPort? {
        (inputPorts + outputPorts).forEach {
            if (cursor.relative().x in it.x..it.x.plus(it.size) && cursor.relative().y in it.y..it.y.plus(it.size)) {
                return it
            }
        }
        return null
    }

    private fun getColor() = if (isMouseOver || dragState == DragState.DRAGGING) {
        activeColor
    } else {
        idleColor
    }

    private fun calculateHeight(): Int {
        return paddingTop + paddingBottom + max(
            inputs.count() * (font.size + textMargin),
            outputs.count() * (font.size + textMargin)
        )
    }

    private fun calculateWidth(): Int {
        sketch.textFont(font)
        val inputsWidth = inputs.maxBy { it.count() }?.let { sketch.textWidth(it).toInt() } ?: 0
        val outputsWidth = outputs.maxBy { it.count() }?.let { sketch.textWidth(it).toInt() } ?: 0
        return inputsWidth + outputsWidth + portSize + 20 // Some space between
    }

    private fun Cursor.relative() = this.copy(x = x - posX, y = y - posY)
}