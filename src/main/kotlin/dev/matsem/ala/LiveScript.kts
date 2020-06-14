//@file:Suppress("UNUSED_LAMBDA_EXPRESSION")

import dev.matsem.ala.generators.LiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.mapSin
import dev.matsem.ala.tools.extensions.translateCenter
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.math.absoluteValue

object : LiveGenerator {

    private lateinit var sketch: PApplet
    private lateinit var canvas: PGraphics

    override fun setup(sketch: PApplet, w: Int, h: Int) {
        this.sketch = sketch
        canvas = sketch.createGraphics(w, h, PConstants.P2D)
    }

    override fun generate(): GeneratorResult {
        canvas.draw {
            translateCenter()
            colorModeHSB()
            clear()
            noStroke()
            val color = lerpColor(
                color(0f, 100f, 100f),
                color(100f, 100f, 100f),
                PApplet.sin(sketch.millis() / 1000f).mapSin(0f, 1f)
            )
            fill(color)
            circle(0f, 0f, (PApplet.sin(sketch.millis() / 500f) * 100f).absoluteValue)
        }

        return GeneratorResult(canvas, BlendMode.ADD)
    }
}