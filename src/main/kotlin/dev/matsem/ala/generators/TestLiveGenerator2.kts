import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.tools.extensions.contrast
import dev.matsem.ala.tools.extensions.draw
import dev.matsem.ala.tools.extensions.setPixel

object : BaseLiveGenerator() {

    override val enabled = true

    val hue = 270f
    val contrast = 2f
    val brightness = -128f

    override fun generate(): GeneratorResult {
        canvas.draw {
            clear()
            colorModeHSB()
            noStroke()
            loadPixels()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color = color(
                        hue,
                        100f,
                        sketch.noise(x.toFloat() / 100f + sketch.millis() / 3000f, y.toFloat()) * 100f
                    ).contrast(contrast, brightness)

                    canvas.setPixel(x, y, color)
                }
            }
            updatePixels()
        }

        return GeneratorResult(canvas, BlendMode.BLEND)
    }
}