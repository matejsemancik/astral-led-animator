package dev.matsem.ala

import processing.core.PApplet

class App {

    private val liveSketch = LiveSketch()

    fun run() {
        val args = arrayOf("LiveSketch")
        PApplet.runSketch(args, liveSketch)
    }
}