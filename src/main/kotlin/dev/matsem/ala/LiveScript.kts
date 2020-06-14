import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.mapSin
import dev.matsem.ala.tools.extensions.translateCenter
import processing.core.PApplet
import kotlin.math.absoluteValue

object : BaseLiveGenerator() {

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