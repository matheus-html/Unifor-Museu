package com.example.app_museu

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class configAPP : Fragment() {

    private lateinit var switchNotifications: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var btnSalvar: Button
    private lateinit var textTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_configapp, container, false)

        switchNotifications = view.findViewById(R.id.switch_notifications)
        switchDarkMode = view.findViewById(R.id.switch_dark_mode)
        btnSalvar = view.findViewById(R.id.btnSalvarConfigAPP)
        textTitle = view.findViewById(R.id.text_title)

        loadSettings()

        btnSalvar.setOnClickListener {
            saveSettings()
        }

        return view
    }

    private fun loadSettings() {
        switchNotifications.isChecked = AppSettings.areNotificationsEnabled(requireContext())
        switchDarkMode.isChecked = AppSettings.isDarkModeEnabled(requireContext())

        AppCompatDelegate.setDefaultNightMode(
            if (switchDarkMode.isChecked) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun saveSettings() {
        val notificationsEnabled = switchNotifications.isChecked
        val darkModeEnabled = switchDarkMode.isChecked

        AppSettings.saveSettings(requireContext(), notificationsEnabled, darkModeEnabled)

        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        requireActivity().recreate()

        Toast.makeText(requireContext(), "Configurações salvas com sucesso.", Toast.LENGTH_SHORT).show()
    }
}
