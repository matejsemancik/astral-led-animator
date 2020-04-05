package dev.matsem.ala

import dev.matsem.ala.tools.extensions.pushPop
import dev.matsem.ala.tools.extensions.translateCenter
import processing.core.PApplet
import processing.core.PConstants

class MainSketch : PApplet() {

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        surface.setTitle("Astral LED Animator")
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
    }

    override fun draw() {
        background(0)
        pushPop {
            translateCenter()
            textAlign(PConstants.CENTER)
            textSize(16f)
            text("So this is it...", 0f, 0f)
        }
    }
}