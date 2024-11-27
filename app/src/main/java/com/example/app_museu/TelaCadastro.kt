package com.example.app_museu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class TelaCadastro : AppCompatActivity() {

    private lateinit var editTextNome: EditText
    private lateinit var editTextNomeDeUsuario: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextSenha: EditText
    private lateinit var buttonCriarConta: Button
    lateinit var fb: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_cadastro)

        editTextNome = findViewById(R.id.cad_nome)
        editTextNomeDeUsuario = findViewById(R.id.cad_nomeUsuario)
        editTextEmail = findViewById(R.id.cad_email)
        editTextSenha = findViewById(R.id.cad_senha)
        buttonCriarConta = findViewById(R.id.button_cadastro)
        fb = Firebase.firestore

        buttonCriarConta.setOnClickListener {
            criarConta()
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

    private fun criarConta() {
        val nome = editTextNome.text.toString()
        val nomeUsuario = editTextNomeDeUsuario.text.toString()
        val email = editTextEmail.text.toString()
        val senha = editTextSenha.text.toString()

        if (nome.isEmpty() || nomeUsuario.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = hashMapOf(
            "nome" to nome,
            "nomeUsuario" to nomeUsuario,
            "email" to email,
            "senha" to senha
        )

        fb.collection("contas")
            .add(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, TelaLogin::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao criar conta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}