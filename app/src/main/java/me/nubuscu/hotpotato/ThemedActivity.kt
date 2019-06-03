package me.nubuscu.hotpotato

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

open class ThemedActivity : AppCompatActivity() {
    private val defaultTheme = "day_mode"
    protected lateinit var currentTheme: String
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPref.getString("theme", defaultTheme) ?: defaultTheme
        setAppTheme(currentTheme)
    }

    override fun onResume() {
        super.onResume()
        val theme = sharedPref.getString("theme", defaultTheme) ?: defaultTheme
        if (currentTheme != theme) {
            recreate()
        }
    }

    private fun setAppTheme(theme: String) {
        val themeId = when (theme) {
            "night_mode" -> R.style.AppTheme_Dark
            else -> R.style.AppTheme_Light // "day_mode"
        }
        setTheme(themeId)
    }
}