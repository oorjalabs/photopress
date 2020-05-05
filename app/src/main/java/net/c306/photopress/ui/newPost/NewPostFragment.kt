package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_post_new.*
import kotlinx.coroutines.*
import net.c306.photopress.MainActivity
import net.c306.photopress.R


class NewPostFragment : Fragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
    private val mTagsAdapter by lazy {
        TagsAutocompleteAdapter(
                context = requireContext(),
                list = newPostViewModel.blogTags.value?.map { it.name }?.toMutableList() ?: mutableListOf()
                               )
    }
    
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {
        return inflater.inflate(R.layout.fragment_post_new, container, false)
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
//            setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
            
            addTextChangedListener(tagsTextWatcher)
            
            setOnFocusChangeListener { v, hasFocus ->
                setInputFocus(
                    v as EditText,
                    hasFocus,
                    R.string.hint_post_tags
                )
            }
        }
        
        input_post_title?.apply {
            setOnFocusChangeListener { v, hasFocus ->
                setInputFocus(
                    v as EditText,
                    hasFocus,
                    R.string.hint_post_title
                )
            }
            addTextChangedListener(titleTextWatcher)
        }
        
        fab_publish?.setOnClickListener {
            newPostViewModel.publishPost()
        }
        
        photo_target?.setOnClickListener {
            openPhotoPicker()
        }
        
        // Load image into view when selected
        newPostViewModel.imageUri.observe(viewLifecycleOwner, Observer {
            setupImageSection(it)
        })
        
        // Show name and link to post when published
        newPostViewModel.publishedPost.observe(viewLifecycleOwner, Observer {
            with (published_post) {
                if (it == null) {
                    visibility = View.GONE
                    text = ""
                } else {
                    visibility = View.VISIBLE
                    text = getString(
                        if (it.isDraft) R.string.message_post_saved_as_draft else R.string.message_post_published,
                        it.post.title,
                        it.post.shortUrl
                    )
                }
            }
        })
        
        // When image is selected, set image name as title if no title is present
        newPostViewModel.fileDetails.observe(viewLifecycleOwner, Observer {
            if (it == null || it.fileName.isBlank() || !newPostViewModel.titleText.value.isNullOrBlank()) return@Observer
            
            setTitleText(it.fileName)
        })
        
        // Update enabled state for inputs based on fragment state
        newPostViewModel.state.observe(viewLifecycleOwner, Observer {
            when (it) {
                
                null,
                NewPostViewModel.State.EMPTY -> {
                    // Enable title and tags inputs
                    input_post_title?.isEnabled = true
                    input_post_tags?.isEnabled = true
                    
                    // Remove photo, add target
                    added_photo?.visibility = View.GONE
                    photo_target?.visibility = View.VISIBLE
                    
                    // Enable photo target
                    photo_target?.isEnabled = true
                    photo_target?.isClickable = true
                    
                    // Disable fab
                    enableFab(false)
                }
                
                NewPostViewModel.State.NO_BLOG_SELECTED -> {
                    // Disable title and tags inputs
                    input_post_title?.isEnabled = false
                    input_post_tags?.isEnabled = false
                    
                    // Remove photo, add target
                    added_photo?.visibility = View.GONE
                    photo_target?.visibility = View.VISIBLE
                    
                    // Disable photo target
                    photo_target?.isEnabled = false
                    photo_target?.isClickable = false
    
                    // Disable fab
                    enableFab(false)
                    
                }
                
                NewPostViewModel.State.HAVE_IMAGE -> {
                    // Disable title and tags inputs
                    input_post_title?.isEnabled = true
                    input_post_tags?.isEnabled = true
    
                    // Remove photo, add target
                    added_photo?.visibility = View.VISIBLE
                    photo_target?.visibility = View.GONE
                    
                    // Disable photo target
                    photo_target?.isEnabled = false
                    photo_target?.isClickable = false
                    
                    // Disable fab
                    enableFab(false)
                }
                
                NewPostViewModel.State.READY -> {
                    // Disable title and tags inputs
                    input_post_title?.isEnabled = true
                    input_post_tags?.isEnabled = true
                    
                    // Remove photo, add target
                    added_photo?.visibility = View.VISIBLE
                    photo_target?.visibility = View.GONE
    
                    // Disable photo target
                    photo_target?.isEnabled = false
                    photo_target?.isClickable = false
    
                    // Enable fab
                    enableFab(true)
                }
                
                NewPostViewModel.State.PUBLISHING -> {
                    // Disable title and tags inputs
                    input_post_title?.isEnabled = false
                    input_post_tags?.isEnabled = false
                    
                    // Remove photo, add target
                    added_photo?.visibility = View.VISIBLE
                    photo_target?.visibility = View.GONE
    
                    // Disable photo target
                    photo_target?.isEnabled = false
                    photo_target?.isClickable = false
                    
                    // Disable fab
                    enableFab(false)
                }
                
                NewPostViewModel.State.PUBLISHED -> {
                    // Clear title and tags text
                    input_post_title?.setText("")
                    input_post_title?.clearFocus()
                    input_post_tags?.setText("")
                    input_post_tags?.clearFocus()
                    
                    // Disable title and tags inputs
                    input_post_title?.isEnabled = false
                    input_post_tags?.isEnabled = false
                    
                    // Remove photo, add target
                    added_photo?.visibility = View.GONE
                    photo_target?.visibility = View.VISIBLE
    
                    // Disable photo target
                    photo_target?.isEnabled = false
                    photo_target?.isClickable = false
                    
                    // Disable fab
                    enableFab(false)
                }
            }
        })
        
        // Show name of blog where we're posting
        newPostViewModel.selectedBlog.observe(viewLifecycleOwner, Observer {
            blog_name.text = if (it == null) {
                getString(R.string.message_no_blog_selected)
            } else {
                getString(R.string.message_posting_to, it.name)
            }
        })
        
        // Update tags in tags suggester
        newPostViewModel.blogTags.observe(viewLifecycleOwner, Observer { list ->
            mTagsAdapter.setList(list?.map { it.name } ?: emptyList())
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
    private fun openPhotoPicker() {
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
    
    
    /**
     * Setup image section based on whether user has selected an image or not
     */
    private fun setupImageSection(imageUri: Uri?) {
        if (imageUri == null) {
            // Hide image view, show drop target
//            added_photo.visibility = View.GONE
//            photo_target.visibility = View.VISIBLE
        } else {
            // Load image into image view
            Glide.with(requireContext())
                //.asGif()
                .load(imageUri)
                // TODO: Based on user settings, this can be set to crop or center
                .optionalFitCenter()
                .into(added_photo)
            // Show image view, hide drop target
//            added_photo.visibility = View.VISIBLE
//            photo_target.visibility = View.GONE
        }
    }
    
    
    private fun enableFab(value: Boolean) {
        fab_publish?.apply {
            if (!value) {
                isEnabled = false
                isClickable = false
                focusable = View.NOT_FOCUSABLE
                backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.bg_fab_disabled))
            } else {
                isEnabled = true
                isClickable = true
                focusable = View.FOCUSABLE_AUTO
                backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.secondaryColor))
            }
        }
    }
    
    
    private val titleTextWatcher = object: TextWatcher {
        
        override fun afterTextChanged(s: Editable?) {
            saveTitleText(s?.toString())
        }
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
        
    }
    
    private var saveTitleJob: Job? = null
    
    /**
     * Save title text to view model after a short delay
     */
    private fun saveTitleText(text: String?) {
        saveTitleJob?.cancel()
        
        saveTitleJob = CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                delay(300)
            }
            newPostViewModel.setTitleText(text)
        }
    }
    
    
    private val tagsTextWatcher = object: TextWatcher {
        
        override fun afterTextChanged(s: Editable?) {
            saveTagsText(s?.toString())
        }
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
        
    }
    
    private var saveTagsJob: Job? = null
    
    /**
     * Save tags to view model after a short delay
     */
    private fun saveTagsText(text: String?) {
        saveTagsJob?.cancel()
        
        saveTagsJob = CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                delay(300)
            }
            newPostViewModel.setPostTags(text)
        }
    }
    
    
    /**
     * Set text in title box, and optionally, mark it selected
     */
    private fun setTitleText(text: String) {
        input_post_title?.apply {
            setText(text)
//            setSelection(0, text.length)
        }
    }
    
    companion object {
        const val RC_PHOTO_PICKER = 9723
    }
    
    class TagsAutocompleteAdapter(context: Context, list: MutableList<String>): ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, list) {
        
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
            }
            else {
                if (text is Spanned) {
                    val sp = SpannableString("$text")
                    TextUtils.copySpansFrom(text, 0, text.length,
                                            Any::class.java, sp, 0)
                    sp
                }
                else {
                    "$text"
                }
            }
        }
    }
}
