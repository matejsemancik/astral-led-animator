package dev.matsem.ala

import processing.core.PApplet

class App {

    private val liveSketch = MainSketch()

    fun run() {
        val args = arrayOf("MainSketch")
        PApplet.runSketch(args, liveSketch)
    }
}