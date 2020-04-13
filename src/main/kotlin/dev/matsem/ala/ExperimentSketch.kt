package dev.matsem.ala

import dev.matsem.ala.tools.extensions.colorModeHSB
import dev.matsem.ala.ui.patch.Patcher
import processing.core.PApplet
import processing.core.PConstants
import java.io.File
import kotlin.random.Random

class ExperimentSketch : PApplet() {

    lateinit var patcher: Patcher

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setResizable(true)
        colorModeHSB()

        patcher = Patcher(this)
        repeat(5) {
            patcher.createPatchBox(
                Random.nextFloat() * width,
                Random.nextFloat() * height
            )
        }
    }

    override fun draw() {
        background(0)
    }

    /**
     * Sketch data path override. It's wrong when using local Processing installation core jars.
     * Sketch folder path cannot be passed as an argument, does not play well with DI.
     */
    override fun dataPath(where: String): String {
        return System.getProperty("user.dir") + "/data/" + where
    }

    /**
     * Sketch data path override. It's wrong when using local Processing installation core jars.
     * Sketch folder path cannot be passed as an argument, does not play well with DI.
     */
    override fun dataFile(where: String): File {
        return File(System.getProperty("user.dir") + "/data/" + where)
    }
}