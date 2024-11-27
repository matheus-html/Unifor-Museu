package com.example.app_museu

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference

object ObrasRepository {

    val listaObras = mutableListOf<Obra>()
    private val favoritos = mutableSetOf<String>()
    val listaObrasAdmin = mutableListOf<Obra>()
    private val db = FirebaseFirestore.getInstance()

    fun inicializarObras() {
        if (listaObras.isEmpty()) {
            listaObras.addAll(
                listOf(
                    Obra(
                    )
                )
            )

            listaObras.forEach { obra ->
                adicionarObraNoFirebase(obra)
            }
        }
    }

    private fun adicionarObraNoFirebase(obra: Obra) {
        val obraRef = db.collection("obras")

        obraRef.whereEqualTo("titulo", obra.titulo)
            .whereEqualTo("autor", obra.autor)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    obraRef.add(obra)
                        .addOnSuccessListener {
                            println("Obra adicionada com sucesso: ${obra.titulo}")
                        }
                        .addOnFailureListener { e ->
                            println("Erro ao adicionar obra: $e")
                        }
                } else {
                    println("A obra '${obra.titulo}' já está no Firebase.")
                }
            }
            .addOnFailureListener { e ->
                println("Erro ao verificar obra no Firebase: $e")
            }
    }

    fun addToFavoritos(titulo: String) {
        if (!favoritos.contains(titulo)) {
            favoritos.add(titulo)
        }
    }

    fun removeFromFavoritos(obraTitulo: String) {
        favoritos.remove(obraTitulo)
    }

    fun isFavorito(obraTitulo: String): Boolean {
        return favoritos.contains(obraTitulo)
    }

    fun getFavoritos(): Set<String> {
        return favoritos
    }

    fun adicionarObraAdmin(obra: Obra) {
        obra.isAdminAdded = true
        listaObrasAdmin.add(obra)
    }

}
