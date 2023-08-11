package net.c306.photopress.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import net.c306.photopress.R
import net.c306.photopress.database.PostImage
import net.c306.photopress.databinding.ItemGalleryPostBinding
import net.c306.photopress.ui.custom.BindingAdapters

class GalleryAdapter(caller: Caller, private val handler: GalleryInteraction) :
    RecyclerView.Adapter<GalleryAdapter.ImageItemViewHolder>() {
    
    enum class Caller {
        NEW_POST_GALLERY,
        REORDER_SCREEN
    }
    
    interface GalleryInteraction {
        fun onImagePressed(image: PostImage)
    }
    
    private var list: List<PostImage> = emptyList()
    private var featuredImageId: Int? = null
    
    private val imageContentDescription = when (caller) {
        Caller.NEW_POST_GALLERY -> R.string.image_item_cd_photo_attributes_gallery
        Caller.REORDER_SCREEN   -> R.string.image_item_cd_photo_attributes_reorder
    }
    
    internal fun setList(newList: List<PostImage>) {
        list = newList
        notifyDataSetChanged()
    }
    
    internal fun setFeaturedImage(imageId: Int?) {
        featuredImageId = imageId
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(
            ItemGalleryPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    
    override fun getItemCount(): Int = list.size
    
    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        list.getOrNull(position)?.also {
            holder.bind(
                image = it,
                isFeaturedImage = featuredImageId == it.id,
                imageContentDescription = imageContentDescription,
                imageCount = list.size,
                handler = handler
            )
        }
    }
    
    class ImageItemViewHolder(private val binding: ItemGalleryPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            image: PostImage,
            isFeaturedImage: Boolean,
            imageContentDescription: Int,
            imageCount: Int,
            handler: GalleryInteraction
        ) {
            with(binding.addedPhoto) {
                setOnClickListener { handler.onImagePressed(image) }
                tooltipText = context.getString(imageContentDescription)
                contentDescription = context.getString(imageContentDescription)
                BindingAdapters.loadImage(
                    view = this,
                    imageUri = image.uri,
                    imageUriCover = null,
                    placeHolderDrawable = null
                )
                isVisible = image?.uri != null
            }
            
            binding.pinnedIcon.isVisible = isFeaturedImage
            binding.tvGalleryItemCaption.text = image.caption.orEmpty()
            binding.tvGalleryItemCaption.isVisible = image.caption != null && imageCount >= 2
            binding.root.tag = image
        }
    }
}