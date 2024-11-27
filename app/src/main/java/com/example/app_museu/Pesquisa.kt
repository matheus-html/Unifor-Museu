package com.example.app_museu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_museu.databinding.FragmentPesquisaBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Pesquisa : Fragment(R.layout.fragment_pesquisa) {

    private var _binding: FragmentPesquisaBinding? = null
    private val binding get() = _binding!!

    private lateinit var pesquisaAdapter: PesquisaAdapter
    private lateinit var fb: FirebaseFirestore
    private var filtroSelecionado: String = "titulo" // Default filter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPesquisaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fb = FirebaseFirestore.getInstance()

        pesquisaAdapter = PesquisaAdapter(emptyList(), object : ObraClickListener {
            override fun onClick(obra: Obra) {
                val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString("obra_titulo", obra.titulo)
                editor.putString("obra_autor", obra.autor)
                editor.putString("obra_data", obra.data)
                editor.putString("obra_tema", obra.tema)
                editor.putString("obra_descricao", obra.descricao)
                editor.putString("obra_cover", obra.cover) // Armazenando a imagem (URL ou caminho)
                editor.apply()

                val intent = Intent(requireContext(), Tela_saiba_mais::class.java)
                startActivity(intent)
            }
        })


        binding.recyclerViewObras.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewObras.adapter = pesquisaAdapter

        binding.searchView.queryHint = "Pesquisar por autor, obra, data ou tema"
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pesquisarObras(newText ?: "")
                return true
            }
        })

        // Configurando os botões de filtro
        binding.btnTitulo.setOnClickListener { filtroSelecionado = "titulo"; binding.searchView.queryHint = "Pesquisar por título" }
        binding.btnAutor.setOnClickListener { filtroSelecionado = "autor"; binding.searchView.queryHint = "Pesquisar por autor" }
        binding.btnData.setOnClickListener { filtroSelecionado = "data"; binding.searchView.queryHint = "Pesquisar por data" }
        binding.btnTema.setOnClickListener { filtroSelecionado = "tema"; binding.searchView.queryHint = "Pesquisar por tema" }
    }

    private fun pesquisarObras(query: String) {
        var queryRef: Query = fb.collection("obras")

        if (query.isNotEmpty()) {
            queryRef = queryRef
                .orderBy(filtroSelecionado)
                .startAt(query)
                .endAt("$query\uf8ff")
        }

        queryRef.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "Nenhuma obra encontrada.", Toast.LENGTH_SHORT).show()
                } else {
                    val obras = mutableListOf<Obra>()
                    for (document in documents) {
                        val obra = document.toObject(Obra::class.java)
                        obras.add(obra)
                    }

                    pesquisaAdapter.updateList(obras)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao buscar obras: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
