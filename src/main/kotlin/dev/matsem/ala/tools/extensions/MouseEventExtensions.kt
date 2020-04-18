package dev.matsem.ala.tools.extensions

import processing.core.PConstants
import processing.event.MouseEvent

fun MouseEvent.isLeftPress() = action == MouseEvent.PRESS && button == PConstants.LEFT

fun MouseEvent.isRightPress() = action == MouseEvent.PRESS && button == PConstants.RIGHT

fun MouseEvent.isMiddlePress() = action == MouseEvent.PRESS && button == PConstants.CENTER

fun MouseEvent.isRelease() = action == MouseEvent.RELEASE

fun MouseEvent.isDrag() = action == MouseEvent.DRAG