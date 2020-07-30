package net.c306.photopress.ui.newPost

import android.net.Uri
import okhttp3.MediaType
import kotlin.random.Random

data class PostImage(
    val id: Int = generateId(),
    val uri: Uri,
    val caption: CharSequence? = null,
    val altText: CharSequence? = null,
    val description: CharSequence? = null,
    val name: CharSequence? = null,
    val mimeType: MediaType? = null,
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