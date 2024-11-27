package com.example.app_museu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding

class AudioGuiaAdapter(
    private var obras: List<Obra>,
    private val clickListener: AudioGuia
) : RecyclerView.Adapter<CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellBinding.inflate(from, parent, false)
        return CardViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int = obras.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bindObra(obras[position])
    }
}
