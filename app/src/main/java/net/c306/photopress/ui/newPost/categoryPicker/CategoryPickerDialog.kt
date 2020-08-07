package net.c306.photopress.ui.newPost.categoryPicker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import net.c306.photopress.R
import net.c306.photopress.api.WPCategory
import net.c306.photopress.databinding.DialogCategoryPickerBinding
import net.c306.photopress.ui.newPost.NewPostViewModel
import net.c306.photopress.utils.AuthPrefs
import net.c306.photopress.utils.Utils
import java.util.*
import kotlin.collections.ArrayList

class CategoryPickerDialog : DialogFragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    private val mSearchTextWatcher by lazy { EditTextWatcher() }
    
    private var mSuggestedNewCategory: String? = null
    private lateinit var mCategoryListAdapter: CategoriesAdapter
    private lateinit var binding: DialogCategoryPickerBinding
    
    private var mPostCategories: MutableList<String>
        get() = newPostViewModel.postCategories.toMutableList()
        set(value) {
            newPostViewModel.postCategories = value
        }
    
    private val mOnCategoryClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { adapterView, _, i, _ ->
            
            val category = adapterView.adapter.getItem(i) as String
            val postCategories = mPostCategories
            
            // Add/remove project from list
            if (category in postCategories) {
                postCategories.remove(category)
            } else {
                postCategories.add(category)
            }
            mPostCategories = postCategories
            mCategoryListAdapter.updatePostCategoriesList(postCategories)
        }
    
    // Create a new category from current filter text, then add that to list, and mark it selected
    private val mOnEmptyViewClick = View.OnClickListener {
        
        if (mSuggestedNewCategory.isNullOrBlank()) return@OnClickListener
        
        val newCategory = mSuggestedNewCategory!!
        
        // Save category to storage
        val updatedCategoriesList = AuthPrefs(it.context).addToCategoriesList(
            WPCategory(
                id = Utils.generateId(),
                name = newCategory,
                slug = newCategory.toLowerCase(Locale.getDefault()).replace(" ", "_"),
                parent = -1,
                postCount = 0,
                isLocal = true
            )
        )
        
        // Update adapter with updated category list from storage
        mCategoryListAdapter.updateList(updatedCategoriesList.map { category -> category.name })
        // Update view model's category list
        newPostViewModel.setBlogCategories(updatedCategoriesList)
        
        // Get index of new category in the updated list
        val index = mCategoryListAdapter.getPosition(newCategory)
        
        // Add new category to selected categories, and mark as selected
        if (index > -1) {
            val postCategories = mPostCategories
            postCategories.add(newCategory)
            mPostCategories = postCategories
            mCategoryListAdapter.updatePostCategoriesList(postCategories)
        }
        
        // Clear search text (this should also hide empty view)
        binding.categorySearch.setText("")
        
        // Reset suggested new category
        mSuggestedNewCategory = null
        
    }
    
    
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
        val context = requireContext()
        val allBlogCategories =
            newPostViewModel.blogCategories.value?.map { category -> category.name }
                ?.toMutableList() ?: mutableListOf()
        
        mCategoryListAdapter = CategoriesAdapter(
            context,
            android.R.layout.simple_list_item_1,
            allBlogCategories,
            mPostCategories
        )
        
        val layoutInflater =
            activity?.layoutInflater ?: throw IllegalStateException("Activity cannot be null")
        
        binding = DialogCategoryPickerBinding.inflate(
            layoutInflater,
            null,
            false
        ).apply {
            listOnClickListener = mOnCategoryClickListener
            listAdapter = mCategoryListAdapter
            onSearchTextChanged = mSearchTextWatcher
            onEmptyViewClicked = mOnEmptyViewClick
        }
        
        // Set buttons and title
        return AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle(getString(R.string.category_picker_title))
            .setPositiveButton(getString(R.string.button_text_done), null)
            .setNegativeButton(getString(R.string.button_text_cancel), null)
            .create()
    }
    
    
    internal class CategoriesAdapter(
        context: Context,
        private val resource: Int,
        allCategories: MutableList<String>,
        private val postCategories: MutableList<String>
    ) : ArrayAdapter<String>(context, resource, allCategories) {
        
        private val mInflater: LayoutInflater = LayoutInflater.from(context)
        private var mFilter = ProjectFilter(allCategories)
        
        internal fun updateList(list: List<String>) {
            // Update projects list in adapter
            clear()
            addAll(list)
            
            // Update list in projects filter
            mFilter.setSourceList(list)
            
            // Notify adapter
            notifyDataSetChanged()
        }
        
        
        fun updatePostCategoriesList(list: List<String>) {
            // Update projects list in adapter
            postCategories.clear()
            postCategories.addAll(list)
            
            // Notify adapter
            notifyDataSetChanged()
        }
        
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (convertView ?: mInflater.inflate(resource, null)).apply {
                
                val category = getItem(position)
                               ?: throw Exception("No item at position $position in categories list!")
                
                // Mark selected if project previously selected
                isActivated = category in postCategories
                
                background = context.resources.getDrawable(
                    if (isActivated) SELECTED_ITEM_BACKGROUND_COLOUR_ID
                    else NORMAL_ITEM_BACKGROUND_COLOUR_ID,
                    context.theme
                )
                
                // Set project text
                findViewById<TextView>(android.R.id.text1).text = category
            }
        }
        
        
        override fun getFilter(): Filter = mFilter
        
        private inner class ProjectFilter(objects: List<String>) : Filter() {
            
            private val sourceList = ArrayList(objects)
            
            internal fun setSourceList(list: List<String>) {
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
                        it.toLowerCase(Locale.getDefault()).contains(filterSeq)
                    }
                    
                    values = filteredList
                    count = filteredList.size
                }
            }
            
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                // NOTE: this function is *always* called from the UI thread.
                clear()
                
                @Suppress("UNCHECKED_CAST")
                addAll(results.values as MutableList<String>)
                notifyDataSetChanged()
            }
        }
        
    }
    
    private inner class EditTextWatcher : TextWatcher {
        
        internal var previousText = ""
            private set
        
        private val onFilter = Filter.FilterListener {
            binding.categoryListEmptyView.apply {
                
                visibility = if (it > 0) View.GONE else View.VISIBLE
                
                mSuggestedNewCategory = null
                
                if (it == 0) {
                    // If there isn't a +@# to start, add a + before previousText, show only first word for suggested project name
                    text = when {
                        
                        previousText.isNotBlank() -> {
                            mSuggestedNewCategory = previousText.trim()
                            getString(
                                R.string.dialog_category_list_empty_view,
                                mSuggestedNewCategory
                            )
                        }
                        
                        else                      ->
                            getString(R.string.dialog_category_list_empty_view_no_projects)
                    }
                }
                
            }
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        
        override fun afterTextChanged(s: Editable?) {
            val inputText = s?.trim().toString()
            
            if (inputText == previousText) return
            
            previousText = inputText
            
            mCategoryListAdapter.filter.filter(previousText, onFilter)
        }
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        
    }
    
    companion object {
        private const val SELECTED_ITEM_BACKGROUND_COLOUR_ID = R.color.bg_list_item_activated
        private const val NORMAL_ITEM_BACKGROUND_COLOUR_ID = R.color.transparent
    }
    
}