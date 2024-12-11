package com.example.app_museu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

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
        val db = FirebaseFirestore.getInstance()

        db.collection("obras")
            .whereEqualTo("ehFavorito", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val favoritosObras = mutableListOf<Obra>()
                for (document in querySnapshot.documents) {
                    val obra = document.toObject(Obra::class.java)
                    if (obra != null) {
                        favoritosObras.add(obra)
                    }
                }

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
                        editor.putString("obra_cover", obra.cover)
                        editor.apply()

                        val intent = Intent(requireContext(), Tela_saiba_mais::class.java)
                        startActivity(intent)
                    }
                })

                recyclerView.adapter = favoritosAdapter
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritosFragment", "Erro ao buscar as obras favoritas: ${exception.message}")
            }
    }

    fun removeFavorito(obra: Obra) {
        val db = FirebaseFirestore.getInstance()
        val obraRef = db.collection("obras").document(obra.titulo)
        obraRef.update("ehFavorito", false)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "${obra.titulo} removida dos favoritos", Toast.LENGTH_SHORT).show()
                updateFavoritosList()
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritosFragment", "Erro ao remover a obra dos favoritos: ${exception.message}")
            }
    }


}
