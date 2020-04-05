package dev.matsem.ala.tools.extensions

import kotlin.random.Random

fun IntRange.random() =
    Random.nextInt((endInclusive + 1) - start) + start