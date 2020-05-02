package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_post_new.*
import kotlinx.coroutines.*
import net.c306.photopress.ActivityViewModel
import net.c306.photopress.MainActivity
import net.c306.photopress.R
import timber.log.Timber

class NewPostFragment : Fragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()

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

        input_post_tags.setOnFocusChangeListener { v, hasFocus ->
            (v as TextInputEditText).hint = if (hasFocus) getString(R.string.hint_post_tags) else null
        }

        input_post_title.setOnFocusChangeListener { v, hasFocus ->
            (v as TextInputEditText).hint = if (hasFocus) getString(R.string.hint_post_title) else null
        }

        fab_publish?.setOnClickListener {
            newPostViewModel.publish(
                activityViewModel.selectedBlogId.value!!,
                newPostViewModel.titleText.value!!,
                newPostViewModel.imageUri.value!!
            )
        }

        photo_target?.setOnClickListener {
            openPhotoPicker()
        }

        newPostViewModel.imageUri.observe(viewLifecycleOwner, Observer {
            setupImageSection(it)
        })


        newPostViewModel.publishedPost.observe(viewLifecycleOwner, Observer {
            with (published_post) {
                if (it == null) {
                    visibility = View.GONE
                    text = ""
                } else {
                    visibility = View.VISIBLE
                    text = getString(R.string.post_published_link, it.title, it.shortUrl)
                }

            }
        })


        newPostViewModel.state.observe(viewLifecycleOwner, Observer {
            when (it) {

                null,
                NewPostViewModel.State.EMPTY -> {
                    photo_target.visibility = View.VISIBLE
                    added_photo.visibility = View.GONE

                    input_post_title.isEnabled = true
                    enableFab(false)
                }

                NewPostViewModel.State.HAVE_IMAGE -> {
                    photo_target.visibility = View.GONE
                    added_photo.visibility = View.VISIBLE
                }

                NewPostViewModel.State.READY -> {
                    photo_target.visibility = View.GONE
                    added_photo.visibility = View.VISIBLE
                    enableFab(true)
                }

                NewPostViewModel.State.PUBLISHING -> {
                    input_post_title.isEnabled = false
                    enableFab(false)
                    input_post_title?.clearFocus()
                }
            }
        })

        input_post_title?.addTextChangedListener(textWatcher)
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

        Timber.d("Got response: ${imageUri?.toString() ?: "nada :("}")

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


    private val textWatcher = object: TextWatcher {

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

    companion object {
        const val RC_PHOTO_PICKER = 9723
    }
}
