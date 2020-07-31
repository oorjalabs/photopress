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
import androidx.lifecycle.Observer
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentPostSettingsBinding
import net.c306.photopress.ui.custom.AppBarNoBottomNavFragment
import net.c306.photopress.utils.setInputFocus

class PostSettingsFragment: AppBarNoBottomNavFragment() {
    
    private lateinit var binding: FragmentPostSettingsBinding
    
    @IdRes
    override val myNavId: Int = R.id.imageAttributesFragment
    
    private val newPostViewModel by activityViewModels<NewPostViewModel>()

    private val mTagsAdapter by lazy {
        TagsAutocompleteAdapter(
            context = requireContext(),
            list = newPostViewModel.blogTags.value?.map { it.name }?.toMutableList()
                ?: mutableListOf()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostSettingsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = newPostViewModel
        binding.fragmentHandler = Handler()
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        binding.inputPostTags.apply {
            
            setAdapter(mTagsAdapter)
            setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
            
            setOnFocusChangeListener { v, hasFocus ->
                (v as EditText).setInputFocus(hasFocus, R.string.new_post_hint_post_tags)
            }
        }
    
        // Update tags in tags suggester
        newPostViewModel.blogTags.observe(viewLifecycleOwner, Observer { list ->
            mTagsAdapter.setList(list?.map { it.name } ?: emptyList())
        })
        
        // Close fragment without saving changes
        binding.toolbar.setNavigationOnClickListener { dismiss() }
    }
    
    
    inner class Handler {
        /**
         * Save values to real variables and close fragment
         */
        fun done() {
            // TODO: 31/07/2020 Save post settings to view model
            dismiss()
        }
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