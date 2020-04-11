package dev.matsem.ala.tools.extensions

import ddf.minim.UGen

inline val UGen.value: Float get() = this.lastValues.firstOrNull() ?: 0f