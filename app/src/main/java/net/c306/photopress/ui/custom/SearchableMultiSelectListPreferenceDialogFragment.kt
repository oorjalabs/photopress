package net.c306.photopress.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import net.c306.photopress.R
import java.util.*
import kotlin.collections.ArrayList

class SearchableMultiSelectListPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    
    companion object {
        fun newInstance(key: String): SearchableMultiSelectListPreferenceDialogFragment =
            SearchableMultiSelectListPreferenceDialogFragment().apply {
                arguments = Bundle(1).apply {
                    putString(ARG_KEY, key)
                }
            }
        
        private const val SAVE_STATE_SELECTED = "SearchableMultiSelectListPreference.selected"
        private const val SAVE_ENTRIES = "SearchableMultiSelectListPreference.entries"
    }
    
    private var mEntries: Array<SearchableMultiSelectListPreference.Entry>? = null
    
    private val mEntriesList by lazy {
        mutableListOf<SearchableMultiSelectListPreference.Entry>().apply {
            mEntries?.let { addAll(it) }
        }
    }
    
    private val mSelectedEntries = mutableSetOf<String>()
    
    private val mListAdapter by lazy {
        SearchableListAdapter(
            requireContext(),
            R.layout.item_searchable_list_pref,
            mEntriesList
        )
    }
    
    private val mSearchTextWatcher by lazy { EditTextWatcher() }
    
    private var mEmptyView: TextView? = null
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (savedInstanceState == null) {
            check(preference.entries != null) { "SearchableMultiSelectListPreference requires an entries array." }
            mEntries = preference.entries
            mSelectedEntries.clear()
            mSelectedEntries.addAll(preference.values)
        } else {
            savedInstanceState.getStringArray(SAVE_STATE_SELECTED)?.let {
                mSelectedEntries.clear()
                mSelectedEntries.addAll(it)
            }
            savedInstanceState.getParcelableArray(SAVE_ENTRIES)?.let { savedEntries ->
                if (savedEntries.all { it is SearchableMultiSelectListPreference.Entry }) {
                    @Suppress("UNCHECKED_CAST")
                    mEntries = savedEntries as Array<SearchableMultiSelectListPreference.Entry>
                }
            }
        }
        
    }
    
    
    @SuppressLint("InflateParams")
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        
        val contentView =
            activity?.layoutInflater?.inflate(R.layout.dialog_searchable_list_preference, null)
                ?.apply {
                    // Populate the list and add click listener
                    findViewById<ListView>(R.id.list)?.apply {
                        choiceMode = ListView.CHOICE_MODE_MULTIPLE
                        adapter = mListAdapter
                        
                        setOnItemClickListener { _, v, _, _ ->
                            
                            val view = v as TextView
                            
                            val selectedEntry = view.tag as SearchableMultiSelectListPreference.Entry
                            
                            // We are toggling entries, so using negation
                            val isInSelected = selectedEntry.saveString !in mSelectedEntries
                            
                            @DrawableRes val drawableEnd: Int
                            @DrawableRes val backgroundDrawable: Int
                            
                            if (isInSelected) {
                                drawableEnd = R.drawable.ic_checked
                                backgroundDrawable = R.color.bg_list_item_activated
                                mSelectedEntries.add(selectedEntry.saveString)
                            } else {
                                drawableEnd = 0
                                backgroundDrawable = R.drawable.sl_bg_item_searchable_pref
                                mSelectedEntries.remove(selectedEntry.saveString)
                            }
                            
                            view.background = context.getDrawable(backgroundDrawable)
                            
                            view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableEnd, 0)
                            
                        }
                    }
                    
                    // If dialog message is set, show it below the list else hide it
                    findViewById<TextView>(R.id.message)?.apply {
                        if (preference.message != null) {
                            text = preference.message
                            visibility = View.VISIBLE
                        } else {
                            visibility = View.GONE
                        }
                    }
                    
                    // Filter on text change
                    findViewById<EditText>(R.id.search_entries)?.apply {
                        addTextChangedListener(mSearchTextWatcher)
                    }
                    
                    // Set empty view
                    findViewById<TextView>(R.id.empty_view).apply {
                        mEmptyView = this
                        text = preference.emptyViewText
                    }
                    
                }
        
        builder?.apply {
            setTitle(preference.title)
            setView(contentView)
        }
        
    }
    
    
    override fun getPreference(): SearchableMultiSelectListPreference {
        return super.getPreference() as SearchableMultiSelectListPreference
    }
    
    
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(SAVE_STATE_SELECTED, mSelectedEntries.toTypedArray())
        outState.putParcelableArray(SAVE_ENTRIES, mEntries)
        super.onSaveInstanceState(outState)
    }
    
    
    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            if (preference.callChangeListener(mSelectedEntries)) {
                preference.values = mSelectedEntries
            }
        }
    }
    
    
    inner class SearchableListAdapter(
        context: Context,
        private val resource: Int,
        list: MutableList<SearchableMultiSelectListPreference.Entry>
    ) : ArrayAdapter<SearchableMultiSelectListPreference.Entry>(context, resource, list) {
        
        private var mFilter = ProjectFilter(list)
        private val mInflater: LayoutInflater = LayoutInflater.from(context)
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (convertView ?: mInflater.inflate(resource, null)).apply {
                
                (this as? TextView)?.let {
                    
                    val item = getItem(position) ?: return@let
                    
                    it.text = item.listDisplayString
                    
                    val isInSelected = item.saveString in mSelectedEntries
                    
                    val drawableEnd = if (isInSelected) R.drawable.ic_checked else 0
                    it.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableEnd, 0)
                    
                    it.background = it.context.getDrawable(
                        if (isInSelected) R.color.bg_list_item_activated
                        else R.drawable.sl_bg_item_searchable_pref
                    )
                    
                    it.tag = item
                }
            }
        }
        
        
        override fun getFilter(): Filter = mFilter
        
        
        private inner class ProjectFilter(objects: MutableList<SearchableMultiSelectListPreference.Entry>) : Filter() {
            
            private val sourceList: ArrayList<SearchableMultiSelectListPreference.Entry> = ArrayList(objects)
            
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
                        it.listSearchString.toLowerCase(Locale.getDefault()).contains(filterSeq)
                    }
                    
                    values = filteredList
                    count = filteredList.size
                }
            }
            
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                // NOTE: this function is *always* called from the UI thread.
                clear()
                
                @Suppress("UNCHECKED_CAST")
                addAll(results.values as MutableList<SearchableMultiSelectListPreference.Entry>)
                notifyDataSetChanged()
            }
        }
        
    }
    
    
    private inner class EditTextWatcher : TextWatcher {
        
        internal var previousText = ""
            private set
        
        private val onFilter = Filter.FilterListener {
            mEmptyView?.visibility = if (it > 0) View.GONE else View.VISIBLE
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        
        override fun afterTextChanged(s: Editable?) {
            val inputText = s?.trim().toString()
            
            if (inputText == previousText) {
                return
            }
            
            previousText = inputText
            
            mListAdapter.filter.filter(previousText, onFilter)
        }
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        
    }
    
}