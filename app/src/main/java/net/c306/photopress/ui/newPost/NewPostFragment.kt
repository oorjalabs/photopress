package net.c306.photopress.ui.newPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import net.c306.photopress.R

class NewPostFragment : Fragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {
        val root = inflater.inflate(R.layout.fragment_post_new, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        newPostViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
