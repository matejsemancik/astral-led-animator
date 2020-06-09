@file:Suppress("UNUSED_LAMBDA_EXPRESSION")

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.remap
import dev.matsem.ala.tools.extensions.translateCenter
import processing.core.PGraphics

{ canvas: PGraphics ->
    val sketch = canvas.parent

    canvas.draw {
        colorModeHSB()
        translateCenter()
        clear()
        noStroke()
        fill(sketch.mouseX.remap(0f, sketch.width.toFloat(), 0f, 360f), 100f, 100f)
        circle(0f, 0f, width.toFloat())
    }
}