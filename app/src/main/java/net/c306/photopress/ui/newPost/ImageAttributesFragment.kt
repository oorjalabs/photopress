package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentImageAttributesBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment
import net.c306.photopress.ui.custom.BindingAdapters
import net.c306.photopress.utils.viewBinding

class ImageAttributesFragment : AppBarNoBottomNavFragment(R.layout.fragment_image_attributes) {
    
    private val binding by viewBinding(FragmentImageAttributesBinding::bind)
    private val args by navArgs<ImageAttributesFragmentArgs>()
    
    @IdRes
    override val myNavId: Int = R.id.imageAttributesFragment
    
    private val viewModel by activityViewModels<NewPostViewModel>()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (args.imageId == -1) {
            // This should never happen.
            // Throw exception so I know we're getting here somehow
            throw Exception("No image selected when Image Attributes fragment opened")
        }
        
        // Set editing image from imageId argument
        viewModel.postImages.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) return@observe
            
            val editingImage = it.find { image -> image.id == args.imageId }
                               ?: return@observe
            
            viewModel.editingImage.value = editingImage
        }
        
        viewModel.editingImage.observe(viewLifecycleOwner) {
            BindingAdapters.loadImage(
                view = binding.image,
                imageUri = null,
                imageUriCover = it?.uri,
                placeHolderDrawable = null,
            )
            binding.inputPostTitle.setText(it?.name.orEmpty())
            binding.inputPostCaption.setText(it?.caption.orEmpty())
            binding.inputPostAltText.setText(it?.altText.orEmpty())
            binding.inputPostDescription.setText(it?.description.orEmpty())
        }
        
        viewModel.postFeaturedImageId.observe(viewLifecycleOwner) {
            val isEditingFeaturedImage = it == viewModel.editingImage.value?.id
            binding.featuredIndicator.isVisible = isEditingFeaturedImage
            binding.buttonFeaturedImage.text = getString(
                if (isEditingFeaturedImage) {
                    R.string.image_attributes_button_unset_as_featured_image
                } else {
                    R.string.image_attributes_button_set_as_featured_image
                }
            )
        }
        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.buttonDone.setOnClickListener { done() }
        binding.image.setOnClickListener { openFullImage() }
        binding.buttonFeaturedImage.setOnClickListener { viewModel.toggleFeaturedImage(viewModel.editingImage.value?.id) }
        binding.buttonRemoveImage.setOnClickListener { removeImage() }
    }
    
    /**
     * Save values to real variables and close fragment
     */
    private fun done() {
        viewModel.editingImage.value?.also {
            viewModel.updatePostImage(it)
        }
        dismiss()
    }
    
    private fun removeImage() {
        viewModel.editingImage.value?.also {
            viewModel.removeImage(it)
        }
        dismiss()
    }
    
    private fun openFullImage() {
        viewModel.editingImage.value?.also {
            findNavController().navigate(ImageAttributesFragmentDirections.actionOpenFullPhoto(it))
        }
    }
}