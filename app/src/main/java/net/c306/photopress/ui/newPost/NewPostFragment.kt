package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import net.c306.photopress.AppViewModel
import net.c306.photopress.R
import net.c306.photopress.api.WPBlogPost
import net.c306.photopress.database.PostImage
import net.c306.photopress.databinding.FragmentPostNewBinding
import net.c306.photopress.ui.custom.BottomNavFragment
import net.c306.photopress.ui.newPost.gallery.GalleryAdapter
import net.c306.photopress.utils.Utils
import net.c306.photopress.utils.setInputFocus

class NewPostFragment : BottomNavFragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    private lateinit var binding: FragmentPostNewBinding
    
    private val mHandler = Handler()
    
    private val mGalleryAdapter by lazy { GalleryAdapter(mHandler) }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostNewBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = newPostViewModel
            handler = mHandler
            galleryAdapter = mGalleryAdapter
            avm = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
        }
        return binding.root
    }
    
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.inputPostTitle.setOnFocusChangeListener { v, hasFocus ->
            (v as EditText).setInputFocus(hasFocus, R.string.new_post_hint_post_title)
        }
        
        
        // Show dialog with published post details when done
        newPostViewModel.publishedPost.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(NewPostFragmentDirections.actionShowAfterPublishDialog())
            }
        })
        
        
        newPostViewModel.postImages.observe(viewLifecycleOwner, Observer { postImagesList ->
            if (postImagesList.isNullOrEmpty()) return@Observer
            
            if (postImagesList.any { it.fileDetails == null }) {
                // Update list with file details
                val updatedWithFileDetails = postImagesList.map {
                    if (it.fileDetails != null) return@map it
                    
                    val fileDetails = newPostViewModel.getFileName(it.uri)
                    it.copy(
                        fileDetails = fileDetails,
                        name = fileDetails.fileName
                    )
                }
                
                newPostViewModel.setPostImages(updatedWithFileDetails)
                
                return@Observer
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
                val postCaption = newPostViewModel.postCaption.value
                
                if (imageCaption != postCaption) newPostViewModel.postCaption.value =
                    postImagesList[0].caption
            }
        })
        
        
        // Update image caption from post caption if there is only one image
        newPostViewModel.postCaption.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrBlank() || newPostViewModel.imageCount.value != 1) return@Observer
            
            val image = newPostViewModel.postImages.value?.getOrNull(0) ?: return@Observer
            
            val imageCaption = image.caption
            val postCaption = it
            
            if (imageCaption != postCaption) newPostViewModel.updatePostImage(image.copy(caption = it))
        })
        
        
        // Update enabled state for inputs based on fragment state
        newPostViewModel.state.observe(viewLifecycleOwner, Observer {
            if (it == NewPostViewModel.State.PUBLISHING) {
                // Show publishing progress indicator
                binding.progressPublishing.show()
            } else {
                // Hide publishing progress indicator
                binding.progressPublishing.hide()
            }
        })
        
        
        // Update state when title text changes
        newPostViewModel.postTitle.observe(viewLifecycleOwner, Observer {
            newPostViewModel.updateState()
        })
        
        
        newPostViewModel.publishLiveData.observe(viewLifecycleOwner, Observer {
            
            if (it == null) return@Observer
            
            val (progress, publishResult) = it
            
            if (publishResult?.errorMessage != null) {
                newPostViewModel.setState(NewPostViewModel.State.READY)
                Toast.makeText(requireContext(), publishResult.errorMessage, Toast.LENGTH_LONG)
                    .show()
                return@Observer
            }
            
            // Show message
            binding.messagePublishingStatus.text = progress.statusMessage
            
            if (!progress.finished) return@Observer
            
            if (publishResult?.publishedPost != null) {
                // Post uploaded, show message
                val toastMessageId = when {
                    publishResult.publishedPost.isDraft                                        -> R.string.new_post_toast_uploaded_as_draft
                    publishResult.publishedPost.post.status == WPBlogPost.PublishStatus.FUTURE -> R.string.new_post_toast_post_scheduled
                    else                                                                       -> R.string.new_post_toast_published
                }
                Toast.makeText(requireContext(), toastMessageId, Toast.LENGTH_SHORT).show()
                
                // Update view model, tags, state, etc
                newPostViewModel.onPublishFinished(publishResult)
            }
            
        })
        
        
        // Set featured image to be marked in recyclerview
        newPostViewModel.postFeaturedImageId.observe(viewLifecycleOwner, Observer {
            mGalleryAdapter.setFeaturedImage(it)
        })
        
    }
    
    
    /**
     * Photo picker returns here for pick or add photos
     */
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
                newPostViewModel.addImageUris(uriList)
            } else {
                // Set selected Uris as new list
                newPostViewModel.setImageUris(uriList)
            }
        }
    }
    
    
    /**
     * Public methods that can be called from data binding
     */
    @Suppress("UNUSED_PARAMETER")
    inner class Handler {
        
        /**
         * Open file picker to select file location for syncing
         */
        fun openPhotoPicker(addPhotos: Boolean = false) {
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
         * Open image attributes for the first image in postImages. If there are no images, do nothing and return
         */
        fun openImageAttributes() {
            openImageAttributes(newPostViewModel.postImages.value?.getOrNull(0) ?: return)
        }
        
        /**
         * Open image attributes for editing
         */
        fun openImageAttributes(image: PostImage) {
            findNavController().navigate(NewPostFragmentDirections.actionEditImageAttributes(image.id))
        }
        
        
        fun openPostSettings(view: View) {
            findNavController().navigate(NewPostFragmentDirections.actionEditPostSettings())
        }
        
        fun onPublishPressed(view: View) {
            findNavController().navigate(NewPostFragmentDirections.actionShowPublishOptions())
        }
        
    }
    
    
    companion object {
        const val RC_PHOTO_PICKER = 9723
        const val RC_PHOTO_PICKER_ADD_PHOTOS = 3942
    }
    
}
