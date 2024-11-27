package com.example.app_museu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class TelaLogin : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var fb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_login)

        fb = FirebaseFirestore.getInstance()
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        senhaEditText = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.button_telaLogin)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else {
                autenticarUsuario(email, senha)
            }
        }

        val button_cad: Button = findViewById(R.id.button_criarConta)
        button_cad.setOnClickListener {
            startActivity(Intent(this, TelaCadastro::class.java))
        }

        val button_semLogin: Button = findViewById(R.id.button_semLogin)
        button_semLogin.setOnClickListener {
            startActivity(Intent(this, TelaHome::class.java))
        }
    }

    private fun autenticarUsuario(email: String, senha: String) {
        fb.collection("contas")
            .whereEqualTo("email", email)
            .whereEqualTo("senha", senha)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show()
                    Log.d("TelaLogin", "Nenhum documento encontrado para o email: $email")
                } else {
                    val userDocument = documents.documents[0]
                    val userId = userDocument.id

                    val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("UserId", userId)
                        apply()
                    }

                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, TelaHome::class.java))
                    Log.d("TelaLogin", "Login bem-sucedido para o email: $email")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao acessar o banco de dados: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("TelaLogin", "Erro ao acessar o Firestore: ${exception.message}")
            }
    }
}
