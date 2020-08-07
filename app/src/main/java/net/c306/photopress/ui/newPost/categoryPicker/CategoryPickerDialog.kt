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
    
    companion object {
        private const val SELECTED_ITEM_BACKGROUND_COLOUR_ID = R.color.bg_list_item_activated
        private const val NORMAL_ITEM_BACKGROUND_COLOUR_ID = R.color.transparent
    }
    
    private var mSuggestedNewCategory: String? = null
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    private lateinit var mCategoryListAdapter: CategoriesAdapter
//    private val mCategoryListAdapter: CategoriesAdapter by lazy {
//        context?.let { context ->
//            CategoriesAdapter(
//                context,
//                android.R.layout.simple_list_item_1,
//                mAllCategoriesList,
//                mPostCategoriesList
//            )
//        } ?: throw Exception("No Context found!")
//    }
    
    private val mSearchTextWatcher by lazy { EditTextWatcher() }
    
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
                        
                        previousText.isNotBlank()  -> {
                            mSuggestedNewCategory = previousText.trim()
                            getString(R.string.dialog_category_list_empty_view, mSuggestedNewCategory)
                        }
                        
                        else                       ->
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
    
    private var mAllCategoriesList: MutableList<String> = mutableListOf()
    private var mPostCategoriesList: MutableList<String> = mutableListOf()
    private lateinit var binding: DialogCategoryPickerBinding
    
    private val mOnCategoryClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { adapterView, view, i, _ ->
            
            val category = adapterView.adapter.getItem(i) as String
            
            // Add/remove project from list
            if (category in mPostCategoriesList) {
                mPostCategoriesList.remove(category)
                view.isActivated = false
                view.background = view.context.resources.getDrawable(
                    NORMAL_ITEM_BACKGROUND_COLOUR_ID,
                    view.context.theme
                )
            } else {
                mPostCategoriesList.add(category)
                view.isActivated = true
                view.background = view.context.resources.getDrawable(
                    SELECTED_ITEM_BACKGROUND_COLOUR_ID,
                    view.context.theme
                )
            }
        }
    
    // Create a new category from current filter text, then add that to list, and mark it selected
    private val mOnEmptyViewClick = View.OnClickListener {
        
        if (mSuggestedNewCategory.isNullOrBlank()) return@OnClickListener
        
        val newCategory = mSuggestedNewCategory!!
        
        // Save category to storage
        val updatedList = AuthPrefs(it.context).addToCategoriesList(
            WPCategory(
                id = Utils.generateId(),
                name = newCategory,
                slug = newCategory.toLowerCase(Locale.getDefault()),
                parent = -1,
                postCount = 0,
                isLocal = true
            )
        )
        
        mCategoryListAdapter.apply {
            
            // Update adapter with updated category list from storage
            updateList(updatedList.map { it.name })
            
            // Get index of new category in the updated list
            val index = getPosition(newCategory)
            
            // Add new category to selected categories, and mark as selected
            if (index > -1) {
                mPostCategoriesList.add(newCategory)
                // TODO: Show as selected
//                getView(index, null, listView as ViewGroup).apply {
//                    isActivated = true
//                    background = context.resources.getDrawable(
//                        SELECTED_ITEM_BACKGROUND_COLOUR_ID,
//                        context.theme
//                    )
//                }
            }
        }
        
        // TODO: Update npvm view model's category list
//        categoryListViewModel.list.value =
//            ProjectUtils.getProjectsList(requireContext())
        
        // Clear search text (this should also hide empty view)
        binding.categorySearch.setText("")
        
        // Reset suggested new category
        mSuggestedNewCategory = null
        
    }
    
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
        return activity?.let {
    
            mCategoryListAdapter = CategoriesAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                newPostViewModel.blogCategories.value?.map { it.name }?.toMutableList() ?: mutableListOf(),
                newPostViewModel.postCategories.value?.split(",")?.toMutableList() ?: mutableListOf()
            )
            
            binding = DialogCategoryPickerBinding.inflate(
                it.layoutInflater,
                null,
                false
            ).apply {
                listOnClickListener = mOnCategoryClickListener
                listAdapter = mCategoryListAdapter
                onSearchTextChanged = mSearchTextWatcher
                onEmptyViewClicked = mOnEmptyViewClick
            }
            
            // Set buttons and title
            AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .setTitle(getString(R.string.category_picker_title))
                .setPositiveButton(getString(R.string.button_text_done)) { _, _ ->
                    // TODO: 07/08/2020 Update post categories in view model
                }
                .setNegativeButton(getString(R.string.button_text_cancel), null)
                .create()
            
        } ?: throw IllegalStateException("Activity cannot be null")
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
    
}