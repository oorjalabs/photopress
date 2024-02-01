package net.c306.photopress.ui.custom

import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.c306.photopress.R

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment {

    constructor(): super()
    constructor(@LayoutRes layout: Int): super(layout)

    override fun getTheme(): Int = R.style.AppTheme_BottomSheetDialog

    override fun onResume() {
        super.onResume()

        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.also {

            BottomSheetBehavior.from(it).apply {
                // Disable half open state
                isFitToContents = true
                // Set state to expanded so the full fragment opens
//                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

    }

}