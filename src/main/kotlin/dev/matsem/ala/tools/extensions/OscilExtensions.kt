package dev.matsem.ala.tools.extensions

import ddf.minim.ugens.Oscil

inline val Oscil.lastValue: Float get() = this.lastValues.firstOrNull() ?: 0f