package net.c306.photopress.ui.custom

import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextWatcher
import android.widget.*
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

object BindingAdapters {
    
    /**
     * Given an imageUri, loads it into the ImageView using Glide
     */
    @BindingAdapter("imageUri", "imageUriCover", "placeholder", requireAll = false)
    @JvmStatic
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
    
    
    @BindingAdapter("recyclerViewAdapter")
    @JvmStatic
    fun setRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        adapter?.also { view.adapter = it }
    }
    
    
    @BindingAdapter("listViewAdapter")
    @JvmStatic
    fun setListViewAdapter(view: ListView, adapter: BaseAdapter?) {
        adapter?.also { view.adapter = it }
    }
    
    
    @BindingAdapter("onItemClick")
    @JvmStatic
    fun setOnItemClickListener(view: ListView, listener: AdapterView.OnItemClickListener) {
        view.onItemClickListener = listener
    }
    
    
    @BindingAdapter("onTextChanged")
    @JvmStatic
    fun setOnTextChangedListener(view: EditText, textWatcher: TextWatcher) {
        view.addTextChangedListener(textWatcher)
    }
    
}