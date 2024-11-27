package com.example.app_museu

import android.content.Context

object AppSettings {
    private const val PREFS_NAME = "AppSettings"

    fun saveSettings(context: Context, notifications: Boolean, darkMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("Notifications", notifications)
            putBoolean("DarkMode", darkMode)
            apply()
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("Notifications", true)
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("DarkMode", false)
    }
}
