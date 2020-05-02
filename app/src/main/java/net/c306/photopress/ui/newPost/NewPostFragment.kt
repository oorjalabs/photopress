package net.c306.photopress.ui.newPost

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import net.c306.photopress.MainActivity
import net.c306.photopress.R
import timber.log.Timber

class NewPostFragment : Fragment() {
    
    private val newPostViewModel: NewPostViewModel by activityViewModels()
    
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

        showFab(true)

        fab_publish?.setOnClickListener {
            showFab(false)
        }

        photo_target?.setOnClickListener {
            openPhotoPicker()
        }

        newPostViewModel.imageUri.observe(viewLifecycleOwner, Observer {
            setupImageSection(it)
        })

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
            added_photo.visibility = View.GONE
            photo_target.visibility = View.VISIBLE
        } else {
            // Load image into image view
            Glide.with(requireContext())
                .load(imageUri)
                // TODO: Based on user settings, this can be set to crop or center
                .optionalFitCenter()
                .into(added_photo)
            // Show image view, hide drop target
            added_photo.visibility = View.VISIBLE
            photo_target.visibility = View.GONE
        }
    }


    private fun showFab(show: Boolean) {
        fab_publish?.apply {
            if (!show) {
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

    companion object {
        const val RC_PHOTO_PICKER = 9723
    }
}
