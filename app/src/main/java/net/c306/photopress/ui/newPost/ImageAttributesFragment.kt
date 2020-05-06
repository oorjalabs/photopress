package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentImageAttributesBinding
import net.c306.photopress.ui.custom.NoBottomNavFragment

class ImageAttributesFragment: NoBottomNavFragment() {
    
    private lateinit var binding: FragmentImageAttributesBinding
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageAttributesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = newPostViewModel
        binding.fragmentHandler = this@ImageAttributesFragment
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        /**
         * Close fragment without saving changes
         */
        binding.toolbar.setNavigationOnClickListener { dismiss() }
    }
    
    /**
     * Close fragment
     */
    private fun dismiss() {
        findNavController().popBackStack(R.id.imageAttributesFragment, true)
    }
    
    /**
     * Save values to real variables and close fragment
     */
    fun done() {
        newPostViewModel.imageTitle.value = newPostViewModel.editingImageTitle.value
        newPostViewModel.imageAltText.value = newPostViewModel.editingImageAltText.value
        newPostViewModel.imageCaption.value = newPostViewModel.editingImageCaption.value
        newPostViewModel.imageDescription.value = newPostViewModel.editingImageDescription.value
        dismiss()
    }
    
}