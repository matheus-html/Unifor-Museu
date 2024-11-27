package com.example.app_museu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        showAdminDialog()

        val button_cad: Button = findViewById(R.id.button_cad)

        button_cad.setOnClickListener{
            startActivity(Intent(this, TelaCadastro::class.java))
        }

        val button_login: Button = findViewById(R.id.button_login)

        button_login.setOnClickListener {
                startActivity(Intent(this, TelaLogin::class.java))
        }

        val button_semLogin: Button = findViewById(R.id.button_semLogin)

        button_semLogin.setOnClickListener {
            startActivity(Intent(this, TelaHome::class.java))
        }
    }

    private fun showAdminDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Administração")
        dialogBuilder.setMessage("Você é um administrador?")
            .setCancelable(false)
            .setPositiveButton("Sim") { _, _ ->
                startActivity(Intent(this, tela_admin_inicial::class.java))
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.show()
    }
}
