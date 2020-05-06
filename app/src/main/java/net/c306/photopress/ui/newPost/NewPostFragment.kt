package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_post_new.*
import net.c306.photopress.MainActivity
import net.c306.photopress.R
import net.c306.photopress.databinding.FragmentPostNewBinding


class NewPostFragment : Fragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    private lateinit var binding: FragmentPostNewBinding
    
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
        binding = FragmentPostNewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = newPostViewModel
        binding.fragmentEventHandler = this@NewPostFragment
        return binding.root
    }
    
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Instead load this from view model on login/welcome completion
        (activity as? MainActivity)?.apply {
            findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        }
        
        input_post_tags?.apply {
            
            setAdapter(mTagsAdapter)
            setTokenizer(CommaTokenizer())
            
            setOnFocusChangeListener { v, hasFocus ->
                setInputFocus(
                    v as EditText,
                    hasFocus,
                    R.string.hint_post_tags
                )
            }
        }
        
        input_post_title?.setOnFocusChangeListener { v, hasFocus ->
            setInputFocus(
                v as EditText,
                hasFocus,
                R.string.hint_post_title
            )
        }
        
        
        // Show dialog with published post details when done
        newPostViewModel.publishedPost.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(NewPostFragmentDirections.actionShowAfterPublishDialog())
            }
        })
        
        
        // When image is selected, set image name as title if no title is present
        newPostViewModel.fileDetails.observe(viewLifecycleOwner, Observer {
            if (it == null || it.fileName.isBlank() || !newPostViewModel.titleText.value.isNullOrBlank()) return@Observer
            
            newPostViewModel.titleText.value = it.fileName
        })
        
        
        // Update enabled state for inputs based on fragment state
        newPostViewModel.state.observe(viewLifecycleOwner, Observer {
            if (it == NewPostViewModel.State.PUBLISHING) {
                // Show publishing progress indicator
                progress_publishing?.show()
            } else {
                // Hide publishing progress indicator
                progress_publishing?.hide()
            }
        })
        
        
        // Update tags in tags suggester
        newPostViewModel.blogTags.observe(viewLifecycleOwner, Observer { list ->
            mTagsAdapter.setList(list?.map { it.name } ?: emptyList())
        })
        
        
        // Update state when title text changes
        newPostViewModel.titleText.observe(viewLifecycleOwner, Observer {
            newPostViewModel.updateState()
        })
        
    }
    
    
    /**
     * Set alternate hint for TextInputEditText on focus, and force show keyboard
     */
    private fun setInputFocus(v: EditText, hasFocus: Boolean, @StringRes stringId: Int) {
        if (hasFocus) {
            // Set hint for edit text only on focus. In non focus mode, the hint for edit text layout is shown
            v.hint = getString(stringId)
            
            // Due to capturing first focus tab here, the keyboard isn't shown. So, force it to show
            (v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
                ?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        } else {
            v.hint = null
        }
    }
    
    
    /**
     * Open file picker to select file location for syncing
     */
    fun openPhotoPicker() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        
        startActivityForResult(galleryIntent, RC_PHOTO_PICKER)
    }
    
    
    /**
     * Photo picker returns here
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        
        if (requestCode != RC_PHOTO_PICKER) return
        
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_LONG).show()
            return
        }
        
        val imageUri = data?.data
        
        if (imageUri != null) {
            newPostViewModel.setImageUri(imageUri)
        }
        
    }
    
    
    companion object {
        const val RC_PHOTO_PICKER = 9723
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
    
    
    /**
     * Taken from MultiAutoCompleteTextView.CommaTokenizer and adapted to remove trailing comma and
     * space from the accepted suggestion. This places only selected text, then user can press comm
     * to see more suggestions.
     */
    class CommaTokenizer : MultiAutoCompleteTextView.Tokenizer {
        
        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor
            while (i > 0 && text[i - 1] != ',') {
                i--
            }
            while (i < cursor && text[i] == ' ') {
                i++
            }
            return i
        }
        
        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            val i = text.indexOf(',', cursor)
            val len = text.length
            
            if (i in 0..len)
                return i
            
            return len
        }
        
        override fun terminateToken(text: CharSequence): CharSequence {
            var i = text.length
            while (i > 0 && text[i - 1] == ' ') {
                i--
            }
            return if (i > 0 && text[i - 1] == ',') {
                text
            } else {
                if (text is Spanned) {
                    val sp = SpannableString("$text")
                    TextUtils.copySpansFrom(
                        text, 0, text.length,
                        Any::class.java, sp, 0
                    )
                    sp
                } else {
                    "$text"
                }
            }
        }
    }
}
