package net.c306.photopress.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.c306.photopress.R

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.AppTheme_BottomSheetDialog


    protected abstract val layoutId: Int

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onResume() {
        super.onResume()

        dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.also {

            BottomSheetBehavior.from(it).apply {
                // Disable half open state
                isFitToContents = true
                // Set state to expanded so the full fragment opens
//                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

    }

}
