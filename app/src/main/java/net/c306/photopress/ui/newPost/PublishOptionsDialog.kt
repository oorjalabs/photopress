package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import net.c306.customcomponents.confirmation.ConfirmationDialog
import net.c306.photopress.R
import net.c306.photopress.database.PhotoPressPost
import net.c306.photopress.databinding.DialogPublishOptionBinding
import net.c306.photopress.ui.custom.BaseBottomSheetDialogFragment
import net.c306.photopress.utils.viewBinding
import java.util.*

@AndroidEntryPoint
class PublishOptionsDialog : BaseBottomSheetDialogFragment(R.layout.dialog_publish_option) {

    private val myTag = this::class.java.name

    private val confirmationRC = 9832

    private val viewModel by activityViewModels<NewPostViewModel>()
    private val confirmationViewModel by activityViewModels<ConfirmationDialog.ConfirmationViewModel>()

    private val binding by viewBinding(DialogPublishOptionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scheduleReady.observe(viewLifecycleOwner, Observer {

            if (it != true) return@Observer

            val dateString = DateUtils.formatDateTime(
                view.context,
                viewModel.scheduledDateTime.value!!,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
            )

            findNavController().navigate(
                R.id.confirmationDialog,
                bundleOf(ConfirmationDialog.KEY_CONFIRMATION_DETAILS to ConfirmationDialog.Details(
                    callerTag = myTag,
                    dialogTitle = getString(R.string.schedule_post_title_confirm_schedule_time),
                    dialogMessage = getString(R.string.schedule_post_message_confirm_schedule_time, dateString),
                    requestCode = confirmationRC,
                    positiveButtonTitle = getString(R.string.string_schedule)
                ))
            )
        })

        confirmationViewModel.result.observe(viewLifecycleOwner, Observer {
            if (it == null || it.callerTag != myTag) return@Observer

            confirmationViewModel.reset()

            when (it.requestCode) {
                confirmationRC ->  {
                    if (it.result) {
                        // Publish with schedule and close dialog
                        viewModel.publishPost(PhotoPressPost.PhotoPostStatus.SCHEDULE)
                        dismiss()
                    } else {
                        // Cancel everything
                        viewModel.resetScheduled()
                    }
                }
            }
        })

        viewModel.showTimePicker.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            findNavController().navigate(PublishOptionsDialogDirections.actionGetPublishTime())
        })

        binding.buttonPublishNow.setOnClickListener {
            publish()
        }

        binding.buttonScheduleForLater.setOnClickListener {
            publishScheduled()
        }

        binding.buttonSaveAsDraft.setOnClickListener {
            uploadAsDraft()
        }
    }


    private fun publish() {
        viewModel.publishPost(PhotoPressPost.PhotoPostStatus.PUBLISH)
        dismiss()
    }

    /**
     * Show dialogs to get scheduling date and time
     */
    private fun publishScheduled() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.schedule_post_title_select_publish_date))
            .setSelection(Date().time)
            .build()
        datePicker.addOnPositiveButtonClickListener {
            viewModel.setSchedule(ready = false, dateTime = it, showTimePicker = true)
        }
        datePicker.show(parentFragmentManager, "datePicker")
    }

    private fun uploadAsDraft() {
        viewModel.publishPost(PhotoPressPost.PhotoPostStatus.DRAFT)
        dismiss()
    }
}