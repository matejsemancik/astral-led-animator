
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.*
import processing.core.PApplet.sin

object : BaseLiveGenerator() {

    override val enabled = false

    override fun generate(): GeneratorResult {
        canvas.draw {
            translateCenter()
            colorModeHSB()
            clear()
            noStroke()
            val color = lerpColor(
                color(0f, 100f, 80f),
                color(20f, 100f, 80f),
                sin(sketch.millis() / 1000f).mapSin(0f, 1f)
            )
            fill(color)
            circle(0f, 0f, width.toFloat())

            noFill()
            stroke(0f, 100f, 20f)
            strokeWeight(width / 6f)
            circle(0f, 0f, sketch.saw(1f / 2.2f) * width)
        }

        return GeneratorResult(canvas, BlendMode.ADD)
    }
}