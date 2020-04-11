package dev.matsem.ala.model

import processing.core.PConstants

enum class BlendMode(val id: Int) {
    /**
     * Linear interpolation of colors: C = A*factor + B
     */
    BLEND(PConstants.BLEND),

    /**
     * Additive blending with white clip: C = min(A*factor + B, 255)
     */
    ADD(PConstants.ADD),

    /**
     * Subtractive blending with black clip: C = max(B - A*factor, 0)
     */
    SUBTRACT(PConstants.SUBTRACT),

    /**
     * Only the darkest color succeeds: C = min(A*factor, B)
     */
    DARKEST(PConstants.DARKEST),

    /**
     * LIGHTEST - only the lightest color succeeds: C = max(A*factor, B)
     */
    LIGHTEST(PConstants.LIGHTEST),

    /**
     * Subtract colors from underlying image.
     */
    DIFFERENCE(PConstants.DIFFERENCE),

    /**
     * Similar to DIFFERENCE, but less extreme.
     */
    EXCLUSION(PConstants.EXCLUSION),

    /**
     * Multiply the colors, result will always be darker.
     */
    MULTIPLY(PConstants.MULTIPLY),

    /**
     * Opposite multiply, uses inverse values of the colors.
     */
    SCREEN(PConstants.SCREEN),

    /**
     * A mix of MULTIPLY and SCREEN. Multiplies dark values, and screens light values.
     */
    OVERLAY(PConstants.OVERLAY),

    /**
     * SCREEN when greater than 50% gray, MULTIPLY when lower.
     */
    HARD_LIGHT(PConstants.HARD_LIGHT),

    /**
     * Mix of DARKEST and LIGHTEST. Works like OVERLAY, but not as harsh.
     */
    SOFT_LIGHT(PConstants.SOFT_LIGHT),

    /**
     * Lightens light tones and increases contrast, ignores darks. Called "Color Dodge" in Illustrator and Photoshop.
     */
    DODGE(PConstants.DODGE),

    /**
     * Darker areas are applied, increasing contrast, ignores lights. Called "Color Burn" in Illustrator and Photoshop.
     */
    BURN(PConstants.BURN)
}