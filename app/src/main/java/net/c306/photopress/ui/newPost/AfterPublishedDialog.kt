package net.c306.photopress.ui.newPost

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import net.c306.customcomponents.utils.CommonUtils
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

        binding.buttonSharePost.setOnClickListener {
            viewModel.publishedPost.value?.post?.let { post ->
                CommonUtils.sendSharingIntent(
                    context = it.context,
                    text = post.url,
                    title = post.title,
                    applyTitleFix = true,
                )
            }
            dismiss()
        }

        binding.buttonCopyPostToClipboard.setOnClickListener {
            viewModel.copyPostToClipboard()
            dismiss()
        }

        binding.buttonOpenPostInBrowser.setOnClickListener {
            viewModel.publishedPost.value?.post?.let { post ->
                startActivity(CommonUtils.getIntentForUrl(post.url))
            }
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.newPost()
        super.onDismiss(dialog)
    }
}