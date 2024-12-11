// FavoritosManager.kt
package com.example.app_museu

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FavoritosManager {

    private val db = FirebaseFirestore.getInstance()

    fun adicionarOuRemoverFavorito(userId: String, obraId: String, adicionar: Boolean) {
        val favoritosRef = db.collection("contas").document(userId).collection("Favoritos")

        if (adicionar) {
            favoritosRef.document(obraId).set(mapOf("favorito" to true))
                .addOnSuccessListener {
                    Log.d("Favoritos", "Obra $obraId adicionada aos favoritos.")
                    atualizarStatusFavorito(obraId, true)
                }
                .addOnFailureListener { e ->
                    Log.w("Favoritos", "Erro ao adicionar obra aos favoritos: ", e)
                }
        } else {
            favoritosRef.document(obraId).delete()
                .addOnSuccessListener {
                    Log.d("Favoritos", "Obra $obraId removida dos favoritos.")
                    atualizarStatusFavorito(obraId, false)
                }
                .addOnFailureListener { e ->
                    Log.w("Favoritos", "Erro ao remover obra dos favoritos: ", e)
                }
        }
    }

    fun atualizarStatusFavorito(obraId: String, favorito: Boolean) {
        val obraRef = db.collection("Obras").document(obraId)

        obraRef.update("ehFavorito", favorito)
            .addOnSuccessListener {
                Log.d("Obras", "Campo 'ehFavorito' atualizado para a obra $obraId.")
            }
            .addOnFailureListener { e ->
                Log.w("Obras", "Erro ao atualizar 'ehFavorito' para a obra $obraId: ", e)
            }
    }

    fun verificarSeFavorito(userId: String, obraId: String, callback: (Boolean) -> Unit) {
        val favoritosRef = db.collection("Usuarios").document(userId).collection("Favoritos")

        favoritosRef.document(obraId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Favoritos", "Erro ao verificar se obra está favoritada: ", e)
                callback(false)
            }
    }

    fun exibirObrasFavoritas(userId: String, callback: (List<String>) -> Unit) {
        val favoritosRef = db.collection("Usuarios").document(userId).collection("Favoritos")

        favoritosRef.get()
            .addOnSuccessListener { result ->
                val obrasFavoritas = mutableListOf<String>()
                for (document in result) {
                    obrasFavoritas.add(document.id)  // Adiciona o ID da obra à lista
                }
                callback(obrasFavoritas)
            }
            .addOnFailureListener { e ->
                Log.w("Favoritos", "Erro ao exibir obras favoritas: ", e)
                callback(emptyList())
            }
    }
}
