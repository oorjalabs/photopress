package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.c306.photopress.AppViewModel
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentPostNewBinding
import net.c306.photopress.ui.custom.BottomNavFragment
import net.c306.photopress.ui.newPost.gallery.GalleryAdapter
import net.c306.photopress.utils.setInputFocus

class NewPostFragment : BottomNavFragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    private lateinit var binding: FragmentPostNewBinding
    
    private val mHandler = BindingHandler()
    
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
                    
                    it.copy(fileDetails = newPostViewModel.getFileName(it.uri))
                }
                
                newPostViewModel.setPostImages(updatedWithFileDetails)
                
                return@Observer
            }
            
            // All images already have file details
            
            if (postImagesList.size > 1) {
                // Update gallery adapter
                mGalleryAdapter.setList(postImagesList)
            } else {
                
                // When only one image is selected, set image name as title if no title is present
                val image = postImagesList[0].fileDetails!!
                
                if (image.fileName.isBlank()) return@Observer
                
                if (newPostViewModel.postTitle.value.isNullOrBlank()) {
                    newPostViewModel.postTitle.value = image.fileName
                }
                if (newPostViewModel.imageTitle.value.isNullOrBlank()) {
                    newPostViewModel.imageTitle.value = image.fileName
                }
            }
            
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
        
    }
    
    
    /**
     * Photo picker returns here
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        
        if (requestCode != RC_PHOTO_PICKER) return
        
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(requireContext(), R.string.new_post_toast_image_selection_cancelled, Toast.LENGTH_LONG).show()
            return
        }
        
        data?.data?.also {imageUri ->
            newPostViewModel.setImageUri(imageUri)
        }
    }
    
    
    companion object {
        const val RC_PHOTO_PICKER = 9723
        
        @BindingAdapter("app:recyclerViewAdapter")
        @JvmStatic
        fun setRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
            adapter?.also { view.adapter = it }
        }
    }
    
    
    /**
     * Public methods that can be called from data binding
     */
    @Suppress("UNUSED_PARAMETER")
    inner class BindingHandler {
        
        /**
         * Open file picker to select file location for syncing
         */
        fun openPhotoPicker() {
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            
            // TODO: 30/07/2020 Implement multiple photo picking https://stackoverflow.com/questions/19585815/select-multiple-images-from-android-gallery
            
            startActivityForResult(galleryIntent, RC_PHOTO_PICKER)
        }
        
        fun openImageAttributes() {
            openImageAttributes(newPostViewModel.postImages.value?.getOrNull(0) ?: return)
        }
        
        fun openImageAttributes(image: PostImage) {
            // TODO: 30/07/2020 use selected image
            newPostViewModel.editingImageTitle.value = newPostViewModel.imageTitle.value
            newPostViewModel.editingImageAltText.value = newPostViewModel.imageAltText.value
            newPostViewModel.editingImageCaption.value = newPostViewModel.imageCaption.value
            newPostViewModel.editingImageDescription.value = newPostViewModel.imageDescription.value
            findNavController().navigate(NewPostFragmentDirections.actionEditImageAttributes())
        }
        
        fun openPostSettings(view: View) {
            findNavController().navigate(NewPostFragmentDirections.actionEditPostSettings())
        }
        
        fun onPublishPressed(view: View) {
            findNavController().navigate(NewPostFragmentDirections.actionShowPublishOptions())
        }
        
    }
    
}
