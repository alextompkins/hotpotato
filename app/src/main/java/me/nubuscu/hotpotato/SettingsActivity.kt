package me.nubuscu.hotpotato

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import me.nubuscu.hotpotato.util.getRoundDrawable
import java.io.File
import java.io.FileNotFoundException


class SettingsActivity : ThemedActivity() {
    private lateinit var avatarUri: Uri
    private lateinit var avatarPreview: ImageView
    private lateinit var chooseAvatarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        avatarUri = File(filesDir, "avatar").toUri()

        avatarPreview = findViewById(R.id.avatarPreview)
        if (avatarUri.toFile().exists()) {
            updateAvatarFrom(avatarUri)
        }

        chooseAvatarButton = findViewById(R.id.chooseAvatarButton)
        chooseAvatarButton.setOnClickListener { this.chooseNewAvatar() }
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

    // AVATAR HANDLING
    private fun chooseNewAvatar() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .setRequestedSize(500, 500)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            when (resultCode) {
                RESULT_OK -> {
                    copyUriToUri(result.uri, avatarUri)
                    updateAvatarFrom(avatarUri)
                }
                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> Toast.makeText(
                    this,
                    "Error when choosing new avatar: ${result.error}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @Throws(FileNotFoundException::class)
    fun copyUriToUri(sourceUri: Uri, destURI: Uri) {
        contentResolver.openInputStream(sourceUri).use { input ->
            contentResolver.openOutputStream(destURI).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun updateAvatarFrom(uri: Uri) {
        val roundDrawable = getRoundDrawable(resources, contentResolver, uri)
        avatarPreview.setImageDrawable(roundDrawable)
    }
}
