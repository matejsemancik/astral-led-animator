package dev.matsem.ala.ui.patch

sealed class PatchPort(open val name: String, open val x: Float, open val y: Float, open val size: Float)

data class InputPort(
    override val name: String,
    override val x: Float,
    override val y: Float,
    override val size: Float
) : PatchPort(name, x, y, size)

data class OutputPort(
    override val name: String,
    override val x: Float,
    override val y: Float,
    override val size: Float
) : PatchPort(name, x, y, size)