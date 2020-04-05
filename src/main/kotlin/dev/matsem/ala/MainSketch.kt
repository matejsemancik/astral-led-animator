package dev.matsem.ala

import processing.core.PApplet
import processing.core.PConstants

class MainSketch : PApplet() {

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
    }

    override fun draw() {
        background(0)
        pushMatrix()
        translate(width / 2f, height / 2f)
        textAlign(PConstants.CENTER)
        textSize(16f)
        text("So this is it...", 0f, 0f)
        popMatrix()
    }
}