package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentImageAttributesBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment

class ImageAttributesFragment: AppBarNoBottomNavFragment() {
    
    private lateinit var binding: FragmentImageAttributesBinding
    private val args by navArgs<ImageAttributesFragmentArgs>()
    
    @IdRes
    override val myNavId: Int = R.id.imageAttributesFragment
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageAttributesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = newPostViewModel
        binding.fragmentHandler = Handler()
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (args.imageId == -1) {
            // This should never happen.
            // Throw exception so I know we're getting here somehow
            throw Exception("No image selected when Image Attributes fragment opened")
        }
        
        // Set editing image from imageId argument
        newPostViewModel.postImages.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            
            val editingImage = it.find { image -> image.id == args.imageId }
                               ?: return@Observer
            
            newPostViewModel.editingImage.value = editingImage
        })
        
        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }
    }
    
    @Suppress("UNUSED_PARAMETER")
    inner class Handler {
        /**
         * Save values to real variables and close fragment
         */
        fun done(view: View) {
            newPostViewModel.editingImage.value?.also {
                newPostViewModel.updatePostImage(it)
            }
            dismiss()
        }
    }
    
}