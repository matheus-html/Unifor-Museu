package com.example.app_museu

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding

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
        // Removido a lógica de cover
        cardCellBinding.titulo.text = obra.titulo
        cardCellBinding.autor.text = obra.autor
        cardCellBinding.data.text = obra.data
        cardCellBinding.tema.text = obra.tema

        updateFavoriteIcon(obra)

        cardCellBinding.cardView.setOnClickListener {
            clickListener.onClick(obra)
        }

        cardCellBinding.toggleButton2.setOnClickListener {
            toggleFavorite(obra)
        }
    }

    private fun toggleFavorite(obra: Obra) {
        val context = cardCellBinding.root.context
        if (ObrasRepository.isFavorito(obra.titulo)) {
            ObrasRepository.removeFromFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} removida dos favoritos", Toast.LENGTH_SHORT).show()

            (context as? FavoritosFragment)?.removeFavorito(obra)
        } else {
            ObrasRepository.addToFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} adicionada aos favoritos", Toast.LENGTH_SHORT).show()
        }
        updateFavoriteIcon(obra)
    }

    private fun updateFavoriteIcon(obra: Obra) {
        cardCellBinding.toggleButton2.isChecked = ObrasRepository.isFavorito(obra.titulo)
    }
}
