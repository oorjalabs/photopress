package net.c306.photopress.ui.newPost

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentPostSettingsBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment
import net.c306.photopress.utils.setInputFocus
import net.c306.photopress.utils.viewBinding

class PostSettingsFragment : AppBarNoBottomNavFragment(R.layout.fragment_post_settings) {
    
    private val binding by viewBinding(FragmentPostSettingsBinding::bind)
    
    @IdRes
    override val myNavId: Int = R.id.post_settings_fragment
    
    private val viewModel by activityViewModels<NewPostViewModel>()
    
    private val mTagsAdapter by lazy {
        TagsAutocompleteAdapter(
            context = requireContext(),
            list = viewModel.blogTags.value?.map { it.name }?.toMutableList()
                   ?: mutableListOf()
        )
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.inputPostTags.apply {
            
            setAdapter(mTagsAdapter)
            setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
            
            setOnFocusChangeListener { v, hasFocus ->
                (v as EditText).setInputFocus(hasFocus, R.string.post_settings_hint_tags)
            }
        }
        
        // Update tags in tags suggester
        viewModel.blogTags.observe(viewLifecycleOwner) { list ->
            mTagsAdapter.setList(list?.map { it.name } ?: emptyList())
        }
        
        viewModel.postTags.observe(viewLifecycleOwner) {
            binding.inputPostTags.setText(it.orEmpty())
        }
        
        viewModel.postCategoriesDisplayString.observe(viewLifecycleOwner) {
            binding.categories.text = it ?: getString(R.string.string_none)
        }
    
        // Close fragment without saving changes
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.buttonDone.setOnClickListener { done() }
        binding.categoriesCollect.setOnClickListener { openCategoriesPicker() }
    }
    
    
    /**
     * Save values to real variables and close fragment
     */
    private fun done() {
        dismiss()
    }
    
    private fun openCategoriesPicker() {
        findNavController().navigate(PostSettingsFragmentDirections.actionOpenCategoryPicker())
    }
    
    
    class TagsAutocompleteAdapter(context: Context, list: MutableList<String>) :
        ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, list) {
        
        private val mInflater = LayoutInflater.from(context)
        private val layoutResource = android.R.layout.simple_list_item_1
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return (convertView ?: mInflater.inflate(layoutResource, null)).apply {
                
                val wpTag = getItem(position)
                
                findViewById<TextView>(android.R.id.text1)?.apply {
                    text = wpTag
                }
            }
        }
        
        fun setList(value: List<String>) {
            clear()
            addAll(value)
            notifyDataSetChanged()
        }
    }
    
}