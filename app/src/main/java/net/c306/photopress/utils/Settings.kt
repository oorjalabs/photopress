package net.c306.photopress.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * To save and fetch data from SharedPreferences
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class Settings @Inject constructor(
    @ApplicationContext context: Context,
) : BasePrefs() {

    override var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val prefsUpdatedTimestamp = MutableStateFlow(0L)

    @Keep
    private val observer = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        prefsUpdatedTimestamp.value = System.currentTimeMillis()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(observer)
    }

    val selectedBlogId: Flow<Int>
        get() = prefsUpdatedTimestamp
            .mapLatest { getSelectedBlogId() }
            .onStart { getSelectedBlogId() }

    fun setSelectedBlogId(value: Int) {
        prefs.edit {
            if (value < 0) {
                remove(KEY_SELECTED_BLOG_ID)
            } else {
                putStringSet(KEY_SELECTED_BLOG_ID, setOf(value.toString()))
            }
        }
    }

    private fun getSelectedBlogId(): Int = try {
        // Stored as string set in upgraded list preference
        val valueSet = prefs.getStringSet(KEY_SELECTED_BLOG_ID, null) ?: emptySet()
        (if (valueSet.isEmpty()) DEFAULT_BLOG_ID else valueSet.first()).toInt()
    } catch (e: ClassCastException) {
        Timber.w(e, "Using old preference for selectedBlogId.")
        // Stored as string in old list preference
        (prefs.getString(KEY_SELECTED_BLOG_ID, null) ?: DEFAULT_BLOG_ID).toInt()
    }


    val useBlockEditor: Boolean
        get() {
            return try {
                val valueSet = prefs.getStringSet(KEY_PUBLISH_FORMAT, null) ?: emptySet()
                if (valueSet.isEmpty()) DEFAULT_USE_BLOCK_EDITOR else valueSet.first() == PUBLISH_FORMAT_BLOCK
            } catch (e: java.lang.ClassCastException) {
                val value =
                    prefs.getString(KEY_PUBLISH_FORMAT, null) ?: return DEFAULT_USE_BLOCK_EDITOR
                value == PUBLISH_FORMAT_BLOCK
            }
        }


    val addFeaturedImage: Boolean
        get() = prefs.getBoolean(KEY_ADD_FEATURED_IMAGE, DEFAULT_ADD_FEATURED_IMAGE)

    val defaultTags: String
        get() = prefs.getStringSet(KEY_DEFAULT_TAGS, null)?.joinToString(", ") ?: ""

    val defaultCategories: List<String>
        get() = prefs.getStringSet(KEY_DEFAULT_CATEGORIES, null)?.toList() ?: emptyList()


    fun upgradeCustomPreferences() {

        val selectedBlogIdString = prefs.getString(KEY_SELECTED_BLOG_ID, null)
        val selectedBlogIdSet = selectedBlogIdString?.let { setOf(it) } ?: setOf()

        val publishFormatString = prefs.getString(KEY_PUBLISH_FORMAT, null)
        val publishFormatSet = publishFormatString?.let { setOf(it) } ?: setOf()

        prefs.edit(commit = true) {
            remove(KEY_SELECTED_BLOG_ID)
            putStringSet(KEY_SELECTED_BLOG_ID, selectedBlogIdSet)

            remove(KEY_PUBLISH_FORMAT)
            putStringSet(KEY_PUBLISH_FORMAT, publishFormatSet)
        }

    }

    companion object {
        const val KEY_SELECTED_BLOG_ID = "key_selected_blog_id"
        const val KEY_PUBLISH_FORMAT = "key_publish_format"
        const val KEY_ADD_FEATURED_IMAGE = "key_add_featured_image"
        const val KEY_DEFAULT_TAGS = "key_default_tags"
        const val KEY_DEFAULT_CATEGORIES = "key_default_categories"

        const val DEFAULT_USE_BLOCK_EDITOR = true
        const val DEFAULT_ADD_FEATURED_IMAGE = true

        private const val DEFAULT_BLOG_ID = "-1"
        internal const val PUBLISH_FORMAT_BLOCK = "block"
    }
}