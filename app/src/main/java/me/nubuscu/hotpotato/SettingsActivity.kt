package me.nubuscu.hotpotato

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            val themePref = findPreference<ListPreference>("theme")
            themePref?.setOnPreferenceChangeListener { _, newValue ->
                val themeId = when (newValue.toString()) {
                    "night_mode" -> R.style.AppTheme_Dark
                    else -> R.style.AppTheme_Light // "day_mode"

                }
                activity?.apply {
                    setTheme(themeId)
                    recreate()
                }
                true
            }
        }
    }
}
