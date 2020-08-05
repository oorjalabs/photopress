package net.c306.photopress.ui.newPost.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.c306.photopress.database.PostImage
import net.c306.photopress.databinding.ItemGalleryPostBinding
import net.c306.photopress.ui.newPost.NewPostFragment

class GalleryAdapter(private val handler: NewPostFragment.Handler) :
    RecyclerView.Adapter<GalleryAdapter.ImageItemViewHolder>() {
    
    private var list: List<PostImage> = emptyList()
    private var featuredImageId: Int? = null
    
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
                it,
                list.size,
                featuredImageId == it.id,
                handler
            )
        }
    }
    
    class ImageItemViewHolder(private val binding: ItemGalleryPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            image: PostImage,
            imageCount: Int,
            isFeaturedImage: Boolean,
            handler: NewPostFragment.Handler
        ) {
            binding.image = image
            binding.handler = handler
            binding.imageCount = imageCount
            binding.isFeaturedImage = isFeaturedImage
            binding.root.tag = image
            binding.executePendingBindings()
        }
        
    }
}