package net.c306.photopress.ui.postList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import net.c306.photopress.R
import net.c306.photopress.ui.custom.BottomNavFragment

class PostListFragment : BottomNavFragment() {
    
    private val postListViewModel: PostListViewModel by activityViewModels()
    
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {
        return inflater.inflate(R.layout.fragment_post_list, container, false)
    }
}
