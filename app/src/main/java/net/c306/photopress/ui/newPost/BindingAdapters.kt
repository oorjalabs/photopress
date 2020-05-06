package net.c306.photopress.ui.newPost

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingAdapters {
    /**
     * Given an imageUri, loads it into the ImageView using Glide
     */
    @BindingAdapter("imageUri", "placeholder", requireAll = false)
    @JvmStatic
    fun loadImage(view: ImageView, imageUri: Uri?, placeHolderDrawable: Drawable?) {
        Glide.with(view.context)
            //.asGif()
            .load(imageUri)
            .placeholder(placeHolderDrawable)
            // TODO: Based on user settings, this can be set to crop or center
            .optionalFitCenter()
            .into(view)
    }

}