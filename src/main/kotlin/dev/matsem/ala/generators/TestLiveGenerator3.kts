import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Waves
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.*

object : BaseLiveGenerator() {

    override val enabled = true

    lateinit var oscil: Oscil
    lateinit var oscil2: Oscil

    override fun onPatch() {
        super.onPatch()
        oscil = Oscil(0.3f, 1f, Waves.SINE).sinked()
        oscil2 = Oscil(1f, 1f, Waves.SAW).sinked()
    }

    override fun generate(): GeneratorResult {
        canvas.draw {
            colorModeHSB()
            fadeToBlackBy(0.2f)
            noStroke()
            fill(oscil2.value.mapp(200f, 250f), 100f, 100f)
            translateCenter()
            circle(0f, 0f, sketch.saw(1f) * width)
        }

        return GeneratorResult(canvas, BlendMode.ADD)
    }
}