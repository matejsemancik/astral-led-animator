package dev.matsem.ala.tools.extensions

import ddf.minim.analysis.FFT

fun FFT.calcAvg(range: ClosedFloatingPointRange<Float>): Float = calcAvg(range.start, range.endInclusive)