package net.c306.photopress.ui.newPost

import android.net.Uri
import kotlin.random.Random

/**
 * Class stored to save image details.
 * Some properties are `var` so they can be updated using two-way data binding.
 */
data class PostImage(
    val id: Int = generateId(),
    val uri: Uri,
    var caption: CharSequence? = null,
    var altText: CharSequence? = null,
    var description: CharSequence? = null,
    var name: CharSequence? = null,
    val fileDetails: FileDetails? = null
) {
    companion object {
        internal fun generateId(): Int = Random.nextInt(0, Int.MAX_VALUE)
    }
    
    data class FileDetails(
        val fileName: String,
        val mimeType: String
    )
}