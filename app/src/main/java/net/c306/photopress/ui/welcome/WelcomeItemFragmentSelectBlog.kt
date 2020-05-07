package net.c306.photopress.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome_item_select_blog.*
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.R

/**
 * Instances of this class are fragments representing a single object in our collection.
 */
class WelcomeItemFragmentSelectBlog : Fragment() {

    private val activityViewModel by activityViewModels<ActivityViewModel>()

    private val selectBlogViewModel by activityViewModels<SelectBlogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_welcome_item_select_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        selectBlogViewModel.blogList.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                // TODO("This may cause an infinite loop if API request keeps returning null/empty. Should stop after one try.")
                selectBlogViewModel.refreshBlogsList()
                button_select_blog.isEnabled = false
            } else {
                button_select_blog.isEnabled = true
            }
        })

        selectBlogViewModel.selectedBlog.observe(viewLifecycleOwner, Observer {
            subtitle_welcome_select_blog?.text =
                if (it == null) getString(R.string.subtitle_welcome_select_blog)
                else getString(R.string.posting_on_blog, it.name)
        })

        activityViewModel.blogSelected.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                setupComplete()
            }
        })

        button_start?.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionGoToApp())
        }

        button_select_blog?.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionSelectBlog())
        }
        
    }

    private fun setupComplete() {
        group_setup_complete?.visibility = View.VISIBLE
        animation_view_done?.playAnimation()
        button_select_blog.visibility = View.GONE
    }
}
