package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import net.c306.photopress.AppViewModel
import net.c306.photopress.R
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.database.PostImage
import net.c306.photopress.databinding.FragmentPostNewBinding
import net.c306.photopress.ui.custom.BottomNavFragment
import net.c306.photopress.ui.gallery.GalleryAdapter
import net.c306.photopress.utils.Utils
import net.c306.photopress.utils.setInputFocus
import net.c306.photopress.utils.viewBinding

class NewPostFragment : BottomNavFragment(R.layout.fragment_post_new),
                        GalleryAdapter.GalleryInteraction {
    
    private val viewModel: NewPostViewModel by activityViewModels()
    private val avm by activityViewModels<AppViewModel>()
    
    private val binding by viewBinding(FragmentPostNewBinding::bind)
    
    private val mGalleryAdapter by lazy {
        GalleryAdapter(
            GalleryAdapter.Caller.NEW_POST_GALLERY,
            this
        )
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.inputPostTitle.setOnFocusChangeListener { v, hasFocus ->
            (v as EditText).setInputFocus(hasFocus, R.string.new_post_hint_post_title)
        }
        
        // Show dialog with published post details when done
        viewModel.publishedPost.observe(viewLifecycleOwner) {
            if (it != null) {
                findNavController().navigate(NewPostFragmentDirections.actionShowAfterPublishDialog())
            }
        }
        
        viewModel.postImages.observe(viewLifecycleOwner) { postImagesList ->
            if (postImagesList.isNullOrEmpty()) return@observe
    
            if (postImagesList.any { it.fileDetails == null }) {
                // Update list with file details
                val updatedWithFileDetails = postImagesList.map {
                    if (it.fileDetails != null) return@map it
            
                    val fileDetails = viewModel.getFileName(it.uri)
                    it.copy(
                        fileDetails = fileDetails,
                        name = fileDetails.fileName
                    )
                }
        
                viewModel.setPostImages(updatedWithFileDetails)
                return@observe
            }
    
            // All images already have file details
    
            // Update grid layout manager's span count to image count limited by max
            (binding.addedGallery.layoutManager as? StaggeredGridLayoutManager)?.spanCount =
                Utils.calculateColumnCount(postImagesList.size)
    
            // Update gallery adapter
            mGalleryAdapter.setList(postImagesList)
    
            // Set image caption as post caption if there is only one image
            if (postImagesList.size == 1 && !postImagesList[0].caption.isNullOrBlank()) {
                val imageCaption = postImagesList[0].caption
                val postCaption = viewModel.postCaption.value
        
                if (imageCaption != postCaption) viewModel.postCaption.value =
                    postImagesList[0].caption
            }
        }
    
        viewModel.selectedBlog.observe(viewLifecycleOwner) {
            binding.blogName.text = if (it?.name.isNullOrBlank()) {
                getString(R.string.new_post_label_no_blog_selected)
            } else {
                getString(R.string.new_post_label_posting_as_author_to_blog, avm.userDisplayName.value, it!!.name)
            }
        }
        
        // Update image caption from post caption if there is only one image
        viewModel.postCaption.observe(viewLifecycleOwner) {
            if (it.isNullOrBlank() || viewModel.imageCount.value != 1) {
                return@observe
            }
    
            val image = viewModel.postImages.value?.getOrNull(0) ?: return@observe
    
            val imageCaption = image.caption
            val postCaption = it
    
            if (imageCaption != postCaption) {
                viewModel.updatePostImage(image.copy(caption = it))
            }
        }
    
        // Update enabled state for inputs based on fragment state
        viewModel.state.observe(viewLifecycleOwner) {
            if (it == NewPostViewModel.State.PUBLISHING) {
                // Show publishing progress indicator
                binding.progressPublishing.show()
            } else {
                // Hide publishing progress indicator
                binding.progressPublishing.hide()
            }

            binding.photoTarget.isClickable = it == NewPostViewModel.State.EMPTY
            binding.photoTarget.isEnabled = it == NewPostViewModel.State.EMPTY

            if (it == NewPostViewModel.State.READY) {
                binding.buttonUpload.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryColor))
                binding.buttonUpload.isClickable = true
                binding.buttonUpload.isFocusable = true
            } else {
                binding.buttonUpload.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_fab_disabled))
                binding.buttonUpload.isClickable = false
                binding.buttonUpload.isFocusable = false
            }
    
            if (it == NewPostViewModel.State.HAVE_IMAGE || it == NewPostViewModel.State.READY) {
                binding.buttonPostSettings.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryColor))
                binding.buttonPostSettings.isClickable = true
                binding.buttonPostSettings.isFocusable = true
            } else {
                binding.buttonPostSettings.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryColor))
                binding.buttonPostSettings.isClickable = false
                binding.buttonPostSettings.isFocusable = false
            }
            
            binding.scrimPublishing.isVisible = it == NewPostViewModel.State.PUBLISHING
            binding.messagePublishingStatus.isVisible = it == NewPostViewModel.State.PUBLISHING
        }
        
        // Update state when title text changes
        viewModel.postTitle.observe(viewLifecycleOwner) {
            viewModel.updateState()
        }
        
        viewModel.publishLiveData.observe(viewLifecycleOwner) {
    
            if (it == null) return@observe
    
            val (progress, publishResult) = it
    
            if (publishResult?.errorMessage != null) {
                viewModel.setState(NewPostViewModel.State.READY)
                Toast.makeText(
                    requireContext(),
                    "Error: " + publishResult.errorMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
                return@observe
            }
    
            // Show message
            binding.messagePublishingStatus.text = progress.statusMessage
    
            if (!progress.finished) return@observe
    
            if (publishResult?.publishedPost != null) {
                // Post uploaded, show message
                val toastMessageId = when {
                    publishResult.publishedPost.isDraft                                        -> R.string.new_post_toast_uploaded_as_draft
                    publishResult.publishedPost.post.status == WPBlogPost.PublishStatus.FUTURE -> R.string.new_post_toast_post_scheduled
                    else                                                                       -> R.string.new_post_toast_published
                }
                Toast.makeText(requireContext(), toastMessageId, Toast.LENGTH_SHORT).show()
        
                // Update view model, tags, state, etc
                viewModel.onPublishFinished(publishResult)
            }
    
        }
    
        viewModel.inputsEnabled.observe(viewLifecycleOwner) {
            binding.inputPostCaption.isEnabled = it
            binding.inputPostTitle.isEnabled = it
        }
    
        // Set featured image to be marked in recyclerview
        viewModel.postFeaturedImageId.observe(viewLifecycleOwner) {
            mGalleryAdapter.setFeaturedImage(it)
        }
    
        viewModel.imageCount.observe(viewLifecycleOwner) {
            binding.photoTarget.isVisible = it == 0
            binding.addedGallery.isVisible = it > 0
            binding.buttonChangeImage.isVisible = it > 0
            binding.buttonAddMorePhotos.isVisible = it > 0
            binding.buttonReorderPhotos.isVisible = it > 1
            binding.inputPostCaption.hint = if (it < 2) {
                getString(R.string.new_post_placeholder_image_caption)
            } else {
                getString(R.string.new_post_placeholder_gallery_caption)
            }
        }
        
        binding.addedGallery.adapter = mGalleryAdapter
        binding.photoTarget.setOnClickListener { openPhotoPicker(false) }
        binding.buttonChangeImage.setOnClickListener { openPhotoPicker(false) }
        binding.buttonAddMorePhotos.setOnClickListener { openPhotoPicker(true) }
        binding.buttonReorderPhotos.setOnClickListener { openReorderingScreen() }
        binding.buttonUpload.setOnClickListener { onPublishPressed() }   
        binding.buttonPostSettings.setOnClickListener { openPostSettings() }
    
        binding.inputPostTitle.doAfterTextChanged {
            viewModel.postTitle.value = it?.toString().orEmpty()
        }
        binding.inputPostCaption.doAfterTextChanged {
            viewModel.postCaption.value = it?.toString().orEmpty()
        }
    }
    
    override fun onResume() {
        super.onResume()
        binding.inputPostTitle.setText(viewModel.postTitle.value.orEmpty())
        binding.inputPostCaption.setText(viewModel.postCaption.value.orEmpty())
    }
    
    /**
     * Photo picker returns here for pick or add photos
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        
        if (requestCode != RC_PHOTO_PICKER && requestCode != RC_PHOTO_PICKER_ADD_PHOTOS) return
        
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(
                requireContext(),
                R.string.new_post_toast_image_selection_cancelled,
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Clip data contains selected items
        data?.clipData?.also {
            val uriList = mutableListOf<Uri>()
            
            for (i in 0 until it.itemCount) {
                uriList.add(it.getItemAt(i).uri)
            }
            
            if (requestCode == RC_PHOTO_PICKER_ADD_PHOTOS) {
                // Add selected Uris to list
                viewModel.addImageUris(uriList)
            } else {
                // Set selected Uris as new list
                viewModel.setImageUris(uriList)
            }
        }
    }
    
    /**
     * Open file picker to select file location for syncing
     */
    private fun openPhotoPicker(addPhotos: Boolean = false) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        
        startActivityForResult(
            galleryIntent,
            if (addPhotos) RC_PHOTO_PICKER_ADD_PHOTOS else RC_PHOTO_PICKER
        )
    }
    
    /**
     * Open image attributes fragment
     */
    override fun onImagePressed(image: PostImage) {
        findNavController().navigate(NewPostFragmentDirections.actionEditImageAttributes(image.id))
    }
    
    private fun openPostSettings() {
        findNavController().navigate(NewPostFragmentDirections.actionEditPostSettings())
    }
    
    private fun onPublishPressed() {
        findNavController().navigate(NewPostFragmentDirections.actionShowPublishOptions())
    }
    
    
    private fun openReorderingScreen() {
        findNavController().navigate(NewPostFragmentDirections.actionReorderImages())
    }
    
    companion object {
        const val RC_PHOTO_PICKER = 9723
        const val RC_PHOTO_PICKER_ADD_PHOTOS = 3942
    }
}
