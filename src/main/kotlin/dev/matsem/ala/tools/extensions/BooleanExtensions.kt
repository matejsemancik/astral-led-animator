package dev.matsem.ala.tools.extensions

fun Boolean.midiValue(): Int = if (this) 127 else 0