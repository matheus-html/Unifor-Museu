package com.example.app_museu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding
import com.google.firebase.firestore.FirebaseFirestore

class FavoritosAdapter(
    private var obras: MutableList<Obra>,
    private val clickListener: ObraClickListener
) : RecyclerView.Adapter<FavoritosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritosViewHolder {
        val binding = CardCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritosViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: FavoritosViewHolder, position: Int) {
        holder.bindObra(obras[position])
    }

    override fun getItemCount(): Int = obras.size

    fun removeObra(obra: Obra) {
        val position = obras.indexOf(obra)
        if (position != -1) {
            obras.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

class FavoritosViewHolder(
    private val cardCellBinding: CardCellBinding,
    private val clickListener: ObraClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    fun bindObra(obra: Obra) {
        cardCellBinding.titulo.text = obra.titulo
        cardCellBinding.autor.text = obra.autor
        cardCellBinding.data.text = obra.data
        cardCellBinding.tema.text = obra.tema

        val bitmap = decodeBase64ToBitmap(obra.cover)
        if (bitmap != null) {
            cardCellBinding.cover.setImageBitmap(bitmap)
        } else {
            cardCellBinding.cover.setImageResource(R.drawable.default_image) // Placeholder
        }

        cardCellBinding.toggleButton2.isChecked = obra.ehFavorito
        cardCellBinding.cardView.setOnClickListener {
            clickListener.onClick(obra)
        }

        cardCellBinding.toggleButton2.setOnCheckedChangeListener { _, isChecked ->
            toggleFavorite(obra, isChecked)
        }
    }


    private fun toggleFavorite(obra: Obra, isFavorito: Boolean) {
        val context = cardCellBinding.root.context
        obra.ehFavorito = isFavorito
        updateFavoritoNoFirestore(obra)

        if (isFavorito) {
            ObrasRepository.addToFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} adicionada aos favoritos", Toast.LENGTH_SHORT).show()
        } else {
            ObrasRepository.removeFromFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} removida dos favoritos", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateFavoritoNoFirestore(obra: Obra) {
        val db = FirebaseFirestore.getInstance()
        db.collection("obras").whereEqualTo("titulo", obra.titulo).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    db.collection("obras").document(documentId)
                        .update("ehFavorito", obra.ehFavorito)
                        .addOnSuccessListener {
                            Log.d("FavoritosViewHolder", "Campo 'ehFavorito' atualizado com sucesso para ${obra.titulo}")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FavoritosViewHolder", "Erro ao atualizar campo 'ehFavorito' para ${obra.titulo}: ${exception.message}")
                        }
                } else {
                    Log.e("FavoritosViewHolder", "Documento da obra não encontrado para ${obra.titulo}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritosViewHolder", "Erro ao buscar o documento da obra: ${exception.message}")
            }
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
}
