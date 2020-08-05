package net.c306.photopress.utils

object Utils {
    internal fun calculateColumnCount(imageCount: Int) = when (imageCount) {
        1 -> 1
        2, 4 -> 2
        else -> 3
    }
}
