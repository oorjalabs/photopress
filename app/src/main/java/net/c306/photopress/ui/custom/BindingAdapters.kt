package net.c306.photopress.ui.custom

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

object BindingAdapters {
    
    /**
     * Given an imageUri, loads it into the ImageView using Glide
     */
    fun loadImage(
        view: ImageView,
        imageUri: Uri?,
        imageUriCover: Uri?,
        placeHolderDrawable: Drawable?
    ) {
        var glideBuilder = Glide.with(view.context)
            //.asGif()
            .load(imageUri ?: imageUriCover)
            .placeholder(placeHolderDrawable)
        
        if (imageUri != null) {
            glideBuilder = glideBuilder.optionalFitCenter()
        } else if (imageUriCover != null) {
            glideBuilder = glideBuilder.optionalCenterCrop()
        }
        glideBuilder.into(view)
    }
}