package net.c306.photopress.ui.newPost

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import net.c306.photopress.AppViewModel
import net.c306.photopress.R
import net.c306.photopress.core.extensions.viewBinding
import net.c306.photopress.databinding.DialogAfterPublishBinding
import net.c306.photopress.ui.custom.BaseBottomSheetDialogFragment

@AndroidEntryPoint
class AfterPublishedDialog : BaseBottomSheetDialogFragment(R.layout.dialog_after_publish) {

    private val viewModel by activityViewModels<NewPostViewModel>()

    private val binding by viewBinding(DialogAfterPublishBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.highlightActionTitle.text = viewModel.publishedDialogMessage

        ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
            .isWordPressAppInstalled
            .observe(viewLifecycleOwner) {
                binding.buttonOpenPostInWordpressApp.isVisible = it
            }

        binding.buttonSharePost.setOnClickListener {
            viewModel.sharePost()
        }

        binding.buttonCopyPostToClipboard.setOnClickListener {
            viewModel.copyPostToClipboard()
        }

        binding.buttonOpenPostInBrowser.setOnClickListener {
            viewModel.openPostExternal()
        }

        binding.buttonOpenPostInWordpressApp.setOnClickListener {
            viewModel.openPostInWordPress()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.newPost()
        super.onDismiss(dialog)
    }

}