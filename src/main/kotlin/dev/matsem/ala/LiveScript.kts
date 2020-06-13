@file:Suppress("UNUSED_LAMBDA_EXPRESSION")

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.shorterDimension
import dev.matsem.ala.tools.extensions.translateCenter
import processing.core.PGraphics

{ canvas: PGraphics ->
    val sketch = canvas.parent

    canvas.draw {
        colorModeHSB()
        translateCenter()
        clear()
        fill(0f, 100f, 100f, 40f)
        rotateY(sketch.millis() / 1000f)
        rotateZ(sketch.millis() / 2000f)
        strokeWeight(10f)
        stroke(20f, 100f, 100f)
        sphereDetail(10)
        sphere(shorterDimension() / 2f)
    }
}