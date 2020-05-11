package net.c306.photopress.ui.newPost

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import net.c306.photopress.R
import java.util.*


class TimeChooserDialogFragment : DialogFragment() {
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()
    
    private val date = Calendar.getInstance()
    
    private var isCancel = false
    
    override fun getTheme(): Int = R.style.AppTheme_DateChooserDialog
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        newPostViewModel.scheduledDateTime.value?.let {
            // Set to date selected in date picker
            date.time = Date(it)
            
            // Set time to now
            val now = Calendar.getInstance()
            date.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
            date.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        }
        
        return TimePickerDialog(
            requireContext(),
            R.style.AppTheme_DateChooserDialog,
            { _, hourOfDay, minute ->
                // On positive selection, update scheduled time and tell VM we are ready
                date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                date.set(Calendar.MINUTE, minute)
                isCancel = false
            },
            date.get(Calendar.HOUR_OF_DAY),
            date.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(requireContext())
        )
    }
    
    override fun onCancel(dialog: DialogInterface) {
        isCancel = true
        super.onCancel(dialog)
    }
    
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        newPostViewModel.setSchedule(
            ready = !isCancel,
            dateTime = if (isCancel) -1 else date.timeInMillis,
            showTimePicker = false
        )
    }
    
}
