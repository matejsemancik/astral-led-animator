
import ddf.minim.UGen
import ddf.minim.ugens.Multiplier
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Waves
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.*

object : BaseLiveGenerator() {

    override val enabled = false

    lateinit var hue: UGen
    lateinit var contrast: UGen
    lateinit var brightness: UGen
    lateinit var speed: UGen

    override fun onPatch() {
        super.onPatch()
        hue = Oscil(0.01f, 1f, Waves.SAW).patch(Multiplier(360f)).sinked()
        contrast = patchBox.slider2.patch(Multiplier(5f)).sinked()
        brightness = patchBox.slider3.sinked()
        speed = patchBox.slider4.sinked()
    }

    override fun generate(): GeneratorResult {
        canvas.draw {
            clear()
            colorModeHSB()
            noStroke()
            loadPixels()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color = color(
                        hue.value,
                        100f,
                        sketch.noise(x.toFloat() / 100f + sketch.millis() / (3000f * speed.value), y.toFloat()) * 100f
                    ).contrast(contrast.value, brightness.value.mapp(-128f, 128f))

                    canvas.setPixel(x, y, color)
                }
            }
            updatePixels()
        }

        return GeneratorResult(canvas, BlendMode.BLEND)
    }
}