package net.c306.photopress.ui.custom

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Given an imageUri, loads it into the ImageView using Glide
 */
fun ImageView.loadImage(
    imageUri: Uri? = null,
    imageUriCover: Uri? = null,
    placeHolderDrawable: Drawable? = null,
) {
    var glideBuilder = Glide.with(context)
        //.asGif()
        .load(imageUri ?: imageUriCover)
        .placeholder(placeHolderDrawable)
    
    if (imageUri != null) {
        glideBuilder = glideBuilder.optionalFitCenter()
    } else if (imageUriCover != null) {
        glideBuilder = glideBuilder.optionalCenterCrop()
    }
    glideBuilder.into(this)
}