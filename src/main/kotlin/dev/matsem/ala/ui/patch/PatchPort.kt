package dev.matsem.ala.ui.patch

sealed class PatchPort(name: String, x: Float, y: Float, size: Float)

data class InputPort(val name: String, val x: Float, val y: Float, val size: Float) : PatchPort(name, x, y, size)

data class OutputPort(val name: String, val x: Float, val y: Float, val size: Float) : PatchPort(name, x, y, size)