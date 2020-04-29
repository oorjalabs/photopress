package net.c306.photopress.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import net.c306.photopress.R

class SettingsFragment : PreferenceFragmentCompat() {
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
    
}
