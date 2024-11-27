package com.example.app_museu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var messageTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)
        recyclerView = view.findViewById(R.id.favoritos_rv)
        messageTextView = view.findViewById(R.id.message_text_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        updateFavoritosList()
        return view
    }

    fun updateFavoritosList() {
        val favoritosTitles = ObrasRepository.getFavoritos()
        val favoritosObras = ObrasRepository.listaObras.filter { obra ->
            favoritosTitles.contains(obra.titulo)
        }.toMutableList()

        if (favoritosObras.isEmpty()) {
            messageTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            messageTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        favoritosAdapter = FavoritosAdapter(favoritosObras, object : ObraClickListener {
            override fun onClick(obra: Obra) {
                val sharedPref = requireContext().getSharedPreferences("AppMuseuPrefs", 0)
                val editor = sharedPref.edit()
                editor.putString("obra_titulo", obra.titulo)
                editor.putString("obra_autor", obra.autor)
                editor.putString("obra_data", obra.data)
                editor.putString("obra_tema", obra.tema)
                editor.putString("obra_descricao", obra.descricao)

                editor.apply()

                val intent = Intent(requireContext(), Tela_saiba_mais::class.java)
                startActivity(intent)
            }
        })

        recyclerView.adapter = favoritosAdapter
    }

    fun removeFavorito(obra: Obra) {
        favoritosAdapter.removeObra(obra)
        updateFavoritosList()
    }
}
