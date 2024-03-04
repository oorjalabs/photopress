package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.c306.photopress.R
import net.c306.photopress.core.extensions.viewBinding
import net.c306.photopress.databinding.FragmentImageAttributesBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment
import net.c306.photopress.ui.custom.loadImage

@AndroidEntryPoint
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
            binding.image.loadImage(imageUriCover = it?.uri)
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
        binding.buttonFeaturedImage.setOnClickListener {
            viewModel.toggleFeaturedImage(viewModel.editingImage.value?.id)
        }
        binding.buttonRemoveImage.setOnClickListener { removeImage() }

        binding.inputPostTitle.doAfterTextChanged {
            viewModel.editingImage.value = viewModel.editingImage.value?.copy(
                name = it?.toString().orEmpty()
            )
        }
        binding.inputPostCaption.doAfterTextChanged {
            viewModel.editingImage.value = viewModel.editingImage.value?.copy(
                caption = it?.toString().orEmpty()
            )
        }
        binding.inputPostAltText.doAfterTextChanged {
            viewModel.editingImage.value = viewModel.editingImage.value?.copy(
                altText = it?.toString().orEmpty()
            )
        }
        binding.inputPostDescription.doAfterTextChanged {
            viewModel.editingImage.value = viewModel.editingImage.value?.copy(
                description = it?.toString().orEmpty()
            )
        }

        // Also clear/reset these after a post has been published
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.resetState.collectLatest {
                    if (it) {
                        binding.inputPostTitle.setText("")
                        binding.inputPostCaption.setText("")
                        binding.inputPostAltText.setText("")
                        binding.inputPostDescription.setText("")
                        binding.inputPostCaption.hint =
                            getString(R.string.new_post_placeholder_image_caption)
                        binding.featuredIndicator.isVisible = false
                        binding.buttonFeaturedImage.text = getString(
                            R.string.image_attributes_button_set_as_featured_image
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val image = viewModel.editingImage.value
        binding.inputPostTitle.setText(image?.name.orEmpty())
        binding.inputPostCaption.setText(image?.caption.orEmpty())
        binding.inputPostAltText.setText(image?.altText.orEmpty())
        binding.inputPostDescription.setText(image?.description.orEmpty())
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