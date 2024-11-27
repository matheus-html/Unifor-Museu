package com.example.app_museu

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore

class tela_admin_inicial : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var obrasAdapter: ObrasAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_admin_inicial)

        recyclerView = findViewById(R.id.recyclerViewObras)
        recyclerView.layoutManager = LinearLayoutManager(this)
        obrasAdapter = ObrasAdapter(mutableListOf())
        recyclerView.adapter = obrasAdapter

        val buttonAddObra: Button = findViewById(R.id.buttonAddObra)
        buttonAddObra.setOnClickListener {
            val intent = Intent(this, tela_adicionar_obra::class.java)
            startActivity(intent)
        }

        carregarObras()
    }

    private fun carregarObras() {
        db.collection("obras").get()
            .addOnSuccessListener { result ->
                val obrasList = mutableListOf<Obra>()
                for (document in result) {
                    val obra = document.toObject(Obra::class.java)
                    obrasList.add(obra)
                }
                obrasAdapter.updateList(obrasList)
            }
            .addOnFailureListener { exception ->
            }
    }
}
