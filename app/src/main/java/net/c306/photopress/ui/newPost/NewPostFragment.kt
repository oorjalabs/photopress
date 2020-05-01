package net.c306.photopress.ui.newPost

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_post_new.*
import net.c306.photopress.MainActivity
import net.c306.photopress.R

class NewPostFragment : Fragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {
        return inflater.inflate(R.layout.fragment_post_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instead load this from view model on login/welcome completion
        (activity as? MainActivity)?.apply {
            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        }

        input_post_tags.setOnFocusChangeListener { v, hasFocus ->
            (v as TextInputEditText).hint = if (hasFocus) getString(R.string.hint_post_tags) else null
        }

        input_post_title.setOnFocusChangeListener { v, hasFocus ->
            (v as TextInputEditText).hint = if (hasFocus) getString(R.string.hint_post_title) else null
        }

        showFab(true)

        fab_publish?.setOnClickListener {
            showFab(false)
        }
    }


    private fun showFab(show: Boolean) {
//        if (show) fab_publish?.show() else fab_publish?.hide()
        fab_publish?.apply {
            if (!show) {
                isEnabled = false
                isClickable = false
                focusable = View.NOT_FOCUSABLE
                backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.bg_fab_disabled))
            } else {
                isEnabled = true
                isClickable = true
                focusable = View.FOCUSABLE_AUTO
                backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.secondaryColor))
            }
        }
    }
}
