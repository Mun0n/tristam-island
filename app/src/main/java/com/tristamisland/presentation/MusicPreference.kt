package com.tristamisland.presentation

import android.content.Context

/**
 * The listener's choice to keep music off. A new region does not turn it back on.
 */
internal class MusicPreference(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun enabled(): Boolean = preferences.getBoolean(KEY, true)

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY, enabled).apply()
    }

    private companion object {
        const val FILE: String = "tristam_music"
        const val KEY: String = "enabled"
    }
}
