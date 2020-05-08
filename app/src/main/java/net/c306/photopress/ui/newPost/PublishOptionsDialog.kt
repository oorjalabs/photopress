package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import net.c306.photopress.R
import net.c306.photopress.databinding.DialogPublishOptionBinding
import net.c306.photopress.ui.custom.BaseBottomSheetDialogFragment
import net.c306.photopress.ui.custom.ConfirmationDialog
import java.util.*

class PublishOptionsDialog : BaseBottomSheetDialogFragment() {
    
    private val myTag = this::class.java.name
    
    private val confirmationRC = 9832
    
    override val layoutId = R.layout.dialog_publish_option
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    private val confirmationViewModel by activityViewModels<ConfirmationDialog.ConfirmationViewModel>()
    
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
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        newPostViewModel.scheduleReady.observe(viewLifecycleOwner, Observer {
            
            if (it != true) return@Observer
            
            val dateString = DateUtils.formatDateTime(
                view.context,
                newPostViewModel.scheduledDateTime.value!!,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
            )
            
            val confirmation = PublishOptionsDialogDirections.actionGlobalConfirmationDialog(
                callerTag = myTag,
                dialogTitle = getString(R.string.title_confirm_schedule_time),
                dialogMessage = getString(R.string.message_confirm_schedule_time, dateString),
                requestCode = confirmationRC,
                positiveButtonTitle = getString(R.string.string_schedule),
                negativeButtonTitle = getString(R.string.string_cancel)
            )
            findNavController().navigate(confirmation)
        })
    
        confirmationViewModel.result.observe(viewLifecycleOwner, Observer {
            if (it == null || it.callerTag != myTag) return@Observer
            
            confirmationViewModel.setResult(null)
        
            when (it.requestCode) {
                confirmationRC ->  {
                    if (it.result) {
                        // Publish with schedule and close dialog
                        newPostViewModel.publishPost(scheduledTime = newPostViewModel.scheduledDateTime.value)
                        dismiss()
                    } else {
                        // Cancel everything
                        newPostViewModel.setScheduleReady(false)
                    }
                }
            }
        })
    
        newPostViewModel.gotDate.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(PublishOptionsDialogDirections.actionPublishOptionsDialogToTimeChooserDialogFragment())
            }
        })
        
    }
    
    
    @Suppress("UNUSED_PARAMETER")
    inner class Handler {
        
        fun publish(view: View) {
            newPostViewModel.publishPost()
            dismiss()
        }
        
        /**
         * Show dialogs to get scheduling date and time
         */
        fun publishScheduled(view: View) {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.title_select_publish_date))
                .setSelection(Date().time)
                .build()
            datePicker.addOnPositiveButtonClickListener {
                newPostViewModel.setScheduledDateTime(it)
                newPostViewModel.gotDate.value = true
            }
            datePicker.show(parentFragmentManager, "datePicker")
        }
        
        fun uploadAsDraft(view: View) {
            newPostViewModel.publishPost(saveAsDraft = true)
            dismiss()
        }
    }
    
    
}