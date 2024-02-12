package net.c306.photopress.ui.gallery

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import net.c306.photopress.R
import net.c306.photopress.database.PostImage
import net.c306.photopress.databinding.FragmentReorderImagesBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.utils.Utils
import net.c306.photopress.utils.viewBinding
import java.util.*

@AndroidEntryPoint
class ReorderImagesFragment : AppBarNoBottomNavFragment(R.layout.fragment_reorder_images),
                              GalleryAdapter.GalleryInteraction,
                              ReorderImagesSwipeHelper.OnDragActionListener {

    private val binding by viewBinding(FragmentReorderImagesBinding::bind)

    @IdRes
    override val myNavId: Int = R.id.reorderImagesFragment

    private val viewModel by activityViewModels<NewPostViewModel>()

    private val mGalleryAdapter by lazy {
        GalleryAdapter(GalleryAdapter.Caller.REORDER_SCREEN, this)
    }

    private val mDragListener by lazy {
        ItemTouchHelper(ReorderImagesSwipeHelper(this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add drag and drop reordering listener
        mDragListener.attachToRecyclerView(binding.images)

        binding.images.adapter = mGalleryAdapter

        // Set editing image from imageId argument
        viewModel.postImages.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) return@observe

            // Update grid layout manager's span count to image count limited by max
            (binding.images.layoutManager as? StaggeredGridLayoutManager)?.spanCount =
                Utils.calculateColumnCount(it.size)

            // Update gallery adapter
            mGalleryAdapter.setList(it)
        }

        // Set featured image to be marked in recyclerview
        viewModel.postFeaturedImageId.observe(viewLifecycleOwner) {
            mGalleryAdapter.setFeaturedImage(it)
        }

        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.buttonDone.setOnClickListener { done() }

        viewModel.imageCount.observe(viewLifecycleOwner) {
            binding.images.isVisible = it > 0
        }
    }


    override fun onMoved(fromPosition: Int, toPosition: Int): Boolean {
        mGalleryAdapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun dragComplete(startPosition: Int, endPosition: Int) {
        if (startPosition == endPosition) return

        val list = viewModel.postImages.value ?: return

        if (startPosition < endPosition) {
            for (i in startPosition until endPosition) {
                Collections.swap(list, i, i + 1)
            }
        } else {
            for (i in startPosition downTo endPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }

        viewModel.setPostImages(list)
    }

    /**
     * Save values to real variables and close fragment
     */
    fun done() {
        dismiss()
    }

    /**
     * Open image in full view
     */
    override fun onImagePressed(image: PostImage) {
        findNavController().navigate(ReorderImagesFragmentDirections.actionOpenFullPhoto(image))
    }
}