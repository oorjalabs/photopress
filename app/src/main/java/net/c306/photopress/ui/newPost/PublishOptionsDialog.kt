package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import net.c306.photopress.R
import net.c306.photopress.databinding.DialogPublishOptionBinding
import net.c306.photopress.ui.custom.BaseBottomSheetDialogFragment

class PublishOptionsDialog: BaseBottomSheetDialogFragment() {
    
    override val layoutId = R.layout.dialog_publish_option
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        val binding = DialogPublishOptionBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            handler = Handler()
        }
        
        return binding.root
    }
    
    
    inner class Handler {
        fun publish(view: View) {
            newPostViewModel.publishPost()
            dismiss()
        }
        
        fun publishScheduled(view: View) {
//            newPostViewModel.publishPost(scheduleTime = 1)
            // TODO("Show dialogs to get scheduling time, then publish")
        }
    
        fun uploadAsDraft(view: View) {
            newPostViewModel.publishPost(true)
            dismiss()
        }
    }
//    override fun onDismiss(dialog: DialogInterface) {
//        newPostViewModel.newPost()
//        super.onDismiss(dialog)
//    }
    
    
}