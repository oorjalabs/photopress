package net.c306.photopress.ui.welcome

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import net.c306.photopress.R
import net.c306.photopress.api.AuthPrefs
import net.c306.photopress.api.Blog
import java.util.*
import kotlin.collections.ArrayList

class BlogChooserDialogFragment: DialogFragment() {

    private val args by navArgs<BlogChooserDialogFragmentArgs>()

    private val mSearchTextWatcher by lazy { EditTextWatcher() }

    private var mCurrentBlogId: Int = -1
    private var mEmptyView: TextView? = null
    private var mSearchEditText: EditText? = null
    private var mSuggestedNewBlog: String? = null

    private val selectBlogViewModel by activityViewModels<SelectBlogViewModel>()

    private val mBlogChooserListAdapter: BlogAdapter by lazy {
        context?.let {context ->
            val blogList = AuthPrefs(context).getBlogsList()

            BlogAdapter(
                context,
                android.R.layout.simple_list_item_1,
                blogList.toMutableList(),
                args.currentBlogId
            )
        } ?: throw Exception("No Context found!")
    }


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mCurrentBlogId = args.currentBlogId

        return activity?.let {

            // Inflate and set the layout for the dialog (Pass null as the parent view because its going in the dialog layout)
            val contentView = it.layoutInflater.inflate(R.layout.dialog_blog_chooser, null).apply {

                // Create list view - add adapter and click listeners
                val listView = findViewById<ListView>(R.id.blog_list)?.apply {
                    choiceMode = ListView.CHOICE_MODE_SINGLE
                    adapter = mBlogChooserListAdapter

                    setOnItemClickListener { adapterView, view, i, _ ->
                        val blog = adapterView.adapter.getItem(i) as Blog
                        mCurrentBlogId = blog.id
                    }
                }

                // Filter list on text change in search
                findViewById<EditText>(R.id.blog_search)?.apply {
                    mSearchEditText = this
                    addTextChangedListener(mSearchTextWatcher)
                }


                // Save empty view, on click, save new blog
                findViewById<TextView>(R.id.blog_list_empty_view).apply {
                    mEmptyView = this

                    // Create a new blog from current filter text, then add that to list, and mark it selected
//                    setOnClickListener {
//
////                        if (!allowCreateBlogs || mSuggestedNewBlog.isNullOrBlank())
////                            return@setOnClickListener
//
//                        // Save blog to storage, including in recents
//                        val updatedList = BlogUtils.saveBlog(requireContext(), mSuggestedNewBlog!!, true)
//
//                        mBlogChooserListAdapter.apply {
//
//                            // Update adapter with updated blog list from storage
//                            updateList(updatedList)
//
//                            // Get index of new blog in the updated list
//                            val index = getPosition(mSuggestedNewBlog)
//
//                            // Add new blog to selected blogs, and mark as selected
//                            if (index > -1) {
//                                mCurrentBlogList.add(mSuggestedNewBlog!!)
//                                getView(index, null, listView as ViewGroup).apply {
//                                    isActivated = true
//                                    background = context.getDrawable(SELECTED_ITEM_BACKGROUND_COLOUR_ID, context.theme)
//                                }
//                            }
//                        }
//
//                        // Update activity's blog list view model
//                        blogListViewModel.list.value = BlogUtils.getBlogsList(requireContext())
//
//                        // Clear search text (this should also hide empty view)
//                        mSearchEditText?.setText("")
//
//                        // Reset suggested new blog
//                        mSuggestedNewBlog = null
//                    }
                }
            }

            // Set buttons and title
            AlertDialog.Builder(requireContext())
                .setView(contentView)
                .setTitle(getString(R.string.blog_selector_title))
                .setPositiveButton(getString(R.string.button_text_done)) { _, _ ->
                    selectBlogViewModel.setSelectedBlogId(mCurrentBlogId)
                }
                .setNegativeButton(getString(R.string.button_text_cancel), null)
                .create()

        } ?: throw IllegalStateException("Activity cannot be null")

    }


    private inner class EditTextWatcher : TextWatcher {

        internal var previousText = ""
            private set

        private val onFilter = Filter.FilterListener {
            mEmptyView?.apply {

                visibility = if (it > 0) View.GONE else View.VISIBLE

                mSuggestedNewBlog = null

            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val inputText = s?.trim().toString()

            if (inputText == previousText) {
                return
            }

            previousText = inputText

            mBlogChooserListAdapter.filter.filter(previousText, onFilter)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    }


    internal class BlogAdapter(
        context: Context,
        private val resource: Int,
        list: MutableList<Blog>,
        private val currentBlogId: Int?
    ) : ArrayAdapter<Blog>(context, resource, list) {

        private val mInflater: LayoutInflater = LayoutInflater.from(context)
        private var mFilter = BlogFilter(list)


        internal fun updateList(list: MutableList<Blog>) {
            // Update blogs list in adapter
            clear()
            addAll(list)

            // Update list in blogs filter
            mFilter.setSourceList(list)

            // Notify adapter
            notifyDataSetChanged()
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (convertView ?: mInflater.inflate(resource, null)).apply {

                val blog = getItem(position)

                // Mark selected if blog previously selected
                isActivated = currentBlogId != null && blog?.id == currentBlogId

                background = context.getDrawable(R.drawable.sl_bg_item_blog_chooser)

                // Set blog text
                findViewById<TextView>(android.R.id.text1).text = blog?.name
            }
        }


        override fun getFilter(): Filter = mFilter

        private inner class BlogFilter(objects: MutableList<Blog>) : Filter() {

            private val sourceList: ArrayList<Blog> = ArrayList(objects)

            internal fun setSourceList(list: MutableList<Blog>) {
                sourceList.clear()
                sourceList.addAll(list)
            }

            override fun performFiltering(chars: CharSequence): FilterResults {
                val filterSeq = chars.toString().toLowerCase(Locale.getDefault())
                val result = FilterResults()

                if (filterSeq.isBlank()) {
                    // add all objects
                    synchronized(this) {
                        result.values = sourceList
                        result.count = sourceList.size
                    }
                    return result
                }

                return result.apply {

                    // The filtering itself
                    val filteredList = sourceList.filter {
                        it.name.toLowerCase(Locale.getDefault()).contains(filterSeq) ||
                        it.url.toLowerCase(Locale.getDefault()).contains(filterSeq)
                    }

                    values = filteredList
                    count = filteredList.size
                }
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                // NOTE: this function is *always* called from the UI thread.
                clear()

                @Suppress("UNCHECKED_CAST")
                addAll(results.values as MutableList<Blog>)
                notifyDataSetChanged()
            }
        }

    }

}