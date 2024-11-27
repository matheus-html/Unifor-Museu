package com.example.app_museu

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TelaExibirObras : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_exibir_obras)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExibirObras)
        ObrasRepository.inicializarObras()

        recyclerView.adapter = ExibirObrasAdapter(ObrasRepository.listaObras, object : ObraClickListener {
            override fun onClick(obra: Obra) {

            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}

