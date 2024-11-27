package com.example.app_museu

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding
import com.google.firebase.firestore.FirebaseFirestore

class CardViewHolder(
    private val cardCellBinding: CardCellBinding,
    private val clickListener: ObraClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    fun bindObra(obra: Obra) {
        // Configurar os campos de texto
        cardCellBinding.titulo.text = obra.titulo
        cardCellBinding.autor.text = obra.autor
        cardCellBinding.data.text = obra.data
        cardCellBinding.tema.text = obra.tema

        // Atualizar a imagem do campo cover
        val bitmap = decodeBase64ToBitmap(obra.cover)
        if (bitmap != null) {
            cardCellBinding.cover.setImageBitmap(bitmap)
        } else {
            cardCellBinding.cover.setImageResource(R.drawable.default_image) // Placeholder
        }

        // Atualizar o ícone de favoritos
        updateFavoriteIcon(obra)

        // Configurar o clique no card
        cardCellBinding.cardView.setOnClickListener {
            clickListener.onClick(obra)
        }

        // Configurar o botão de áudio guia
        cardCellBinding.audioGuiaBotaoHome.setOnClickListener {
            val context = cardCellBinding.root.context
            val intent = Intent(context, Tela_AudioObra::class.java).apply {
                putExtra("obra_titulo", obra.titulo)
                putExtra("obra_autor", obra.autor)
                putExtra("obra_data", obra.data)
                putExtra("obra_tema", obra.tema)
                putExtra("obra_descricao", obra.descricao)
                putExtra("obra_position", adapterPosition) // Passa a posição da obra
            }
            context.startActivity(intent)
        }
        // Configurar o botão de favoritos
        cardCellBinding.toggleButton2.setOnClickListener {
            toggleFavorite(obra)
        }
    }

    private fun toggleFavorite(obra: Obra) {
        val context = cardCellBinding.root.context
        FirebaseFirestore.getInstance()

        // Inverter o estado do favorito
        val novoEstadoFavorito = !obra.ehFavorito
        obra.ehFavorito = novoEstadoFavorito

        // Atualiza o estado no Firestore
        updateFavoritoNoFirestore(obra)

        if (novoEstadoFavorito) {
            // Adiciona aos favoritos
            ObrasRepository.addToFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} adicionada aos favoritos", Toast.LENGTH_SHORT).show()
        } else {
            // Remove dos favoritos
            ObrasRepository.removeFromFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} removida dos favoritos", Toast.LENGTH_SHORT).show()
        }

        // Atualiza o ícone de favoritos
        updateFavoriteIcon(obra)
    }

    private fun updateFavoriteIcon(obra: Obra) {
        cardCellBinding.toggleButton2.isChecked = obra.ehFavorito
    }

    private fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    private fun updateFavoritoNoFirestore(obra: Obra) {
        val db = FirebaseFirestore.getInstance()

        // Encontre o documento da obra no Firestore com base no título
        db.collection("obras").whereEqualTo("titulo", obra.titulo).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id

                    // Atualize o campo 'ehFavorito' dentro do documento da obra
                    db.collection("obras").document(documentId)
                        .update("ehFavorito", obra.ehFavorito) // Atualiza o campo "ehFavorito"
                        .addOnSuccessListener {
                            Log.d("CardViewHolder", "Campo 'ehFavorito' atualizado com sucesso para ${obra.titulo}")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("CardViewHolder", "Erro ao atualizar campo 'ehFavorito' para ${obra.titulo}: ${exception.message}")
                        }
                } else {
                    Log.e("CardViewHolder", "Documento da obra não encontrado para ${obra.titulo}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CardViewHolder", "Erro ao buscar o documento da obra: ${exception.message}")
            }
    }


}