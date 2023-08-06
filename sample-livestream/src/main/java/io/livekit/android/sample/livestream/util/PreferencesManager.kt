package io.livekit.android.sample.livestream.util

import android.content.Context
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("preferences", 0)

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun setUsername(value: String) = prefs.edit { putString(KEY_USERNAME, value) }

    companion object {
        private const val KEY_USERNAME = "username"
    }
}