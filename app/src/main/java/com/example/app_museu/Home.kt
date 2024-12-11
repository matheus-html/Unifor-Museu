package com.example.app_museu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_museu.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class Home : Fragment(), ObraClickListener {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var adapter: ObraAdapter
    private val fb = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        loadObrasFromFirebase()

        return binding.root
    }

    private fun loadObrasFromFirebase() {
        fb.collection("obras").get()
            .addOnSuccessListener { result ->
                val obras = mutableListOf<Obra>()
                for (document in result) {
                    val obra = document.toObject(Obra::class.java)
                    obras.add(obra)
                }

                val listaCombinada = mutableListOf<Obra>().apply {
                    addAll(obras)
                    addAll(ObrasRepository.listaObrasAdmin)
                }

                adapter = ObraAdapter(listaCombinada, this)
                binding.recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Erro ao carregar obras: ${exception.message}")
            }
    }

    override fun onClick(obra: Obra) {
        requireContext().getSharedPreferences("AppMuseuPrefs", Context.MODE_PRIVATE).edit().apply {
            putString("obra_titulo", obra.titulo)
            putString("obra_autor", obra.autor)
            putString("obra_data", obra.data)
            putString("obra_tema", obra.tema)
            putString("obra_descricao", obra.descricao)
            putString("obra_cover", obra.cover) // Adicione esta linha
            apply()
        }
        startActivity(Intent(requireContext(), Tela_saiba_mais::class.java))
    }

}
