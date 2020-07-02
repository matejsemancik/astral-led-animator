
import ddf.minim.UGen
import ddf.minim.ugens.Oscil
import ddf.minim.ugens.Waves
import dev.matsem.ala.generators.BaseLiveGenerator
import dev.matsem.ala.model.BlendMode
import dev.matsem.ala.model.GeneratorResult
import dev.matsem.ala.tools.extensions.*

object : BaseLiveGenerator() {

    lateinit var oscil: Oscil
    lateinit var frequency: UGen
    lateinit var amplitude: UGen
    lateinit var beamWidth: UGen
    lateinit var hue: UGen
    lateinit var fading: UGen
    lateinit var mod: UGen

    override fun onPatch() {
        super.onPatch()
        oscil = Oscil(1f, 1f, Waves.SAW).sinked()
        frequency = patchBox.knob1.patchedTo(oscil.frequency)
        amplitude = patchBox.knob2.patchedTo(oscil.amplitude)
        beamWidth = patchBox.knob3.sinked()
        hue = patchBox.knob4.sinked()
        fading = patchBox.slider1.sinked()
        mod = patchBox.slider2.sinked()
    }

    override fun generate(): GeneratorResult {
        val beamWidth = beamWidth.value.toInt().constrain(low = 1)
        val mod = mod.value.toInt().constrain(low = 1)

        canvas.noSmooth()
        canvas.draw {
            colorModeHSB()
            pushPop {
                noFill()
                stroke(color(hue.value, 100f, 100f))
                strokeWeight(1f)

                fadeToBlackBy(fading.value)
                val x = oscil.value.mapSin(0f, width.toFloat()).toInt()
                for (y in 0 until height) {
                    if (y % mod == 0) {
                        line(x.toFloat(), y.toFloat(), x + beamWidth.toFloat(), y + 1f)
                    } else {
                        line(width - x.toFloat(), y.toFloat(), width - x - beamWidth.toFloat(), y + 1f)
                    }
                }
            }
        }

        return GeneratorResult(canvas, BlendMode.ADD)
    }
}