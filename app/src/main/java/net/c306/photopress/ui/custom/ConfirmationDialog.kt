package net.c306.photopress.ui.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.navArgs
import net.c306.photopress.R

class ConfirmationDialog: DialogFragment() {
    
    private val confirmationViewModel: ConfirmationViewModel by activityViewModels()
    
    private val args: ConfirmationDialogArgs by navArgs()
    
    override fun getTheme() = R.style.AppTheme
    
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(requireContext())
        
        // Inflate and set the layout for the dialog (Pass null as the parent view because its going in the dialog layout)
        val contentView = activity?.layoutInflater?.inflate(R.layout.dialog_action_confirmation, null)?.apply {
            
            // Show message
            findViewById<TextView>(R.id.action_confirmation_message)?.text = args.dialogMessage
            
            val listView = findViewById<ListView>(R.id.action_confirmation_list) ?: return@apply
            
            if (args.list == null) {
                listView.visibility = View.GONE
            } else {
                // Create list view - add adapter
                listView.apply {
                    visibility = View.VISIBLE
                    adapter = ArrayAdapter(context, R.layout.item_action_confirmation_list, args.list ?: emptyArray())
                }
            }
        }
        
        builder
            .setView(contentView)
            .setTitle(args.dialogTitle)
            .setPositiveButton(args.positiveButtonTitle ?: getString(R.string.string_ok)) { _, _ ->
                confirmationViewModel.setResult(ConfirmationDialogResult(args.requestCode, true, args.callerTag, args.returnBundle))
            }
            .setNegativeButton(args.negativeButtonTitle ?: getString(R.string.string_cancel)) { _, _ ->
                confirmationViewModel.setResult(ConfirmationDialogResult(args.requestCode, false, args.callerTag, args.returnBundle))
            }
        
        if (args.iconResourceId != -1) {
            builder.setIcon(args.iconResourceId)
        }
        
        return builder.create()
    }
    
    
    data class ConfirmationDialogResult(
        val requestCode: Int,
        val result: Boolean,
        val callerTag: String,
        val returnBundle: Bundle? = null
    )
    
    class ConfirmationViewModel : ViewModel() {
        private val _result = MutableLiveData<ConfirmationDialogResult>()
        val result: LiveData<ConfirmationDialogResult> = _result
        
        fun setResult(result: ConfirmationDialogResult?) {
            _result.value = result
        }
    }
    
}