package net.c306.photopress.ui.newPost

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import net.c306.photopress.R
import net.c306.photopress.databinding.DialogAfterPublishBinding
import net.c306.photopress.ui.custom.BaseBottomSheetDialogFragment

class AfterPublishedDialog : BaseBottomSheetDialogFragment() {

    override val layoutId: Int = R.layout.dialog_after_publish

    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DialogAfterPublishBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = newPostViewModel

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        newPostViewModel.newPost()
        super.onDismiss(dialog)
    }
}