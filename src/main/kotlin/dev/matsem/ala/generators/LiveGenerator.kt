package dev.matsem.ala.generators

import dev.matsem.ala.model.GeneratorResult
import processing.core.PApplet

interface LiveGenerator {

    fun setup(sketch: PApplet, w: Int, h: Int)

    fun generate(): GeneratorResult
}