package net.c306.photopress.utils

import kotlin.random.Random

object Utils {
    internal fun calculateColumnCount(imageCount: Int) = when (imageCount) {
        1 -> 1
        2, 4 -> 2
        else -> 3
    }
    
    fun generateId(): Int = Random.nextInt()
}
