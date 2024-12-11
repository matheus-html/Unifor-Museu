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
    private var filtroSelecionado: String = "titulo"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPesquisaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fb = FirebaseFirestore.getInstance()

        carregarObrasVistas()

        pesquisaAdapter = PesquisaAdapter(emptyList(), object : ObraClickListener {
            override fun onClick(obra: Obra) {
                val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString("obra_titulo", obra.titulo)
                editor.putString("obra_autor", obra.autor)
                editor.putString("obra_data", obra.data)
                editor.putString("obra_tema", obra.tema)
                editor.putString("obra_descricao", obra.descricao)
                editor.putString("obra_cover", obra.cover)
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

        binding.btnTitulo.setOnClickListener { filtroSelecionado = "titulo"; binding.searchView.queryHint = "Pesquisar por título" }
        binding.btnAutor.setOnClickListener { filtroSelecionado = "autor"; binding.searchView.queryHint = "Pesquisar por autor" }
        binding.btnData.setOnClickListener { filtroSelecionado = "data"; binding.searchView.queryHint = "Pesquisar por data" }
        binding.btnTema.setOnClickListener { filtroSelecionado = "tema"; binding.searchView.queryHint = "Pesquisar por tema" }

        binding.btnLimparHistorico.setOnClickListener {
            binding.searchView.setQuery("", false)

            val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.remove("obra_titulo")
            editor.remove("obra_autor")
            editor.remove("obra_data")
            editor.remove("obra_tema")
            editor.remove("obras_vistas")
            editor.apply()

            binding.recyclerViewObrasVistas.adapter = null

            Toast.makeText(requireContext(), "Histórico de pesquisa limpo.", Toast.LENGTH_SHORT).show()
        }
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

                    salvarObrasVistas(obras)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao buscar obras: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun salvarObrasVistas(obras: List<Obra>) {
        val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        val obrasVistas = sharedPref.getStringSet("obras_vistas", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        obras.forEach { obra ->
            obrasVistas.add(obra.titulo) 
        }

        editor.putStringSet("obras_vistas", obrasVistas)
        editor.apply()
        carregarObrasVistas()
    }

    private fun carregarObrasVistas() {
        val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
        val obrasVistas = sharedPref.getStringSet("obras_vistas", mutableSetOf())

        if (obrasVistas != null && obrasVistas.isNotEmpty()) {
            val query = fb.collection("obras")
                .whereIn("titulo", obrasVistas.toList())

            query.get()
                .addOnSuccessListener { documents ->
                    val obrasVistasList = mutableListOf<Obra>()
                    for (document in documents) {
                        val obra = document.toObject(Obra::class.java)
                        obrasVistasList.add(obra)
                    }
                    val obrasVistasAdapter = PesquisaAdapter(obrasVistasList, object : ObraClickListener {
                        override fun onClick(obra: Obra) {
                            val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
                            val editor: SharedPreferences.Editor = sharedPref.edit()
                            editor.putString("obra_titulo", obra.titulo)
                            editor.putString("obra_autor", obra.autor)
                            editor.putString("obra_data", obra.data)
                            editor.putString("obra_tema", obra.tema)
                            editor.putString("obra_descricao", obra.descricao)
                            editor.putString("obra_cover", obra.cover)
                            editor.apply()
                            val intent = Intent(requireContext(), Tela_saiba_mais::class.java)
                            startActivity(intent)
                        }
                    })
                    binding.recyclerViewObrasVistas.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerViewObrasVistas.adapter = obrasVistasAdapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Erro ao carregar obras vistas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Limpar RecyclerView quando não houver obras vistas
            binding.recyclerViewObrasVistas.adapter = null
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
