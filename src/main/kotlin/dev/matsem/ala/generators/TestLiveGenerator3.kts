import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Waves
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.audio.FFTListener
import dev.matsem.ala.tools.extensions.*

object : BaseLiveGenerator() {

    lateinit var oscil2: Oscil
    lateinit var oscil3: Oscil
    var fftListener: FFTListener? = null

    override fun onPatch() {
        super.onPatch()
        oscil2 = Oscil(0.01f, 1f, Waves.SINE).sinked()
        oscil3 = Oscil(0.01f, 1f, Waves.SINE).sinked()
    }

    override fun generate(): GeneratorResult {
        canvas.draw {
            colorModeHSB()
            fadeToBlackBy(0.2f)
            noStroke()
            fill(oscil2.value.mapp(0f, 360f), 100f, 100f)
            translateCenter()
            circle(0f, 0f, oscil3.value * width)
        }

        return GeneratorResult(canvas, BlendMode.EXCLUSION)
    }
}