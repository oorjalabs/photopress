package net.c306.photopress.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import net.c306.photopress.R
import net.c306.photopress.database.PostImage
import net.c306.photopress.databinding.FragmentReorderImagesBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.utils.Utils
import java.util.*

class ReorderImagesFragment: AppBarNoBottomNavFragment(), ReorderImagesSwipeHelper.OnDragActionListener {
    
    private lateinit var binding: FragmentReorderImagesBinding
    
    @IdRes
    override val myNavId: Int = R.id.reorderImagesFragment
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    private val mHandler = Handler()
    private val mGalleryAdapter by lazy { GalleryAdapter(GalleryAdapter.Caller.REORDER_SCREEN, mHandler) }
    private val mDragListener by lazy {
        ItemTouchHelper(ReorderImagesSwipeHelper(this))
    }
    
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReorderImagesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = newPostViewModel
        binding.handler = mHandler
        binding.galleryAdapter = mGalleryAdapter
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        // Add drag and drop reordering listener
        mDragListener.attachToRecyclerView(binding.images)
        
        // Set editing image from imageId argument
        newPostViewModel.postImages.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
    
            // Update grid layout manager's span count to image count limited by max
            (binding.images.layoutManager as? StaggeredGridLayoutManager)?.spanCount =
                Utils.calculateColumnCount(it.size)
            
            // Update gallery adapter
            mGalleryAdapter.setList(it)
        })
        
        // Set featured image to be marked in recyclerview
        newPostViewModel.postFeaturedImageId.observe(viewLifecycleOwner, Observer {
            mGalleryAdapter.setFeaturedImage(it)
        })
        
        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }
    }
    
    
    override fun onMoved(fromPosition: Int, toPosition: Int): Boolean {
        mGalleryAdapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }
    
    override fun dragComplete(startPosition: Int, endPosition: Int) {
        if (startPosition == endPosition) return
        
        val list = newPostViewModel.postImages.value ?: return
        
        if (startPosition < endPosition) {
            for (i in startPosition until endPosition) {
                Collections.swap(list, i, i + 1)
            }
        }
        else {
            for (i in startPosition downTo endPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }
        
        newPostViewModel.setPostImages(list)
    }
    
    @Suppress("UNUSED_PARAMETER")
    inner class Handler: GalleryAdapter.GalleryInteraction {
        /**
         * Save values to real variables and close fragment
         */
        fun done(view: View) {
            dismiss()
        }
        
        /**
         * Start drag maybe?
         */
        override fun onImagePressed(image: PostImage) {
            // TODO: 06/08/2020 do nothing or start drag?
        }
    }
    
}