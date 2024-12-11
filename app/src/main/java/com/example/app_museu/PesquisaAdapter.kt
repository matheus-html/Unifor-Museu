package com.example.app_museu

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding

class PesquisaAdapter(
    private var obras: List<Obra>,
    private val clickListener: ObraClickListener
) : RecyclerView.Adapter<CardViewHolder>() {

    private var obrasFiltradas: List<Obra> = obras

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellBinding.inflate(from, parent, false)
        return CardViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int = obrasFiltradas.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bindObra(obrasFiltradas[position])

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val obra = obrasFiltradas[position]
            val intent = Intent(context, Tela_AudioObra::class.java).apply {
                putExtra("obra_titulo", obra.titulo)
                putExtra("obra_autor", obra.autor)
                putExtra("obra_data", obra.data)
                putExtra("obra_tema", obra.tema)
                putExtra("obra_descricao", obra.descricao)
                putExtra("obra_position", position) // Passa a posição da obra
            }
            context.startActivity(intent)

        }
    }
    fun updateList(newObras: List<Obra>) {
        obras = newObras
        obrasFiltradas = newObras
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        obrasFiltradas = if (query.isEmpty()) {
            obras
        } else {
            val lowerCaseQuery = query.lowercase()
            obras.filter { obra ->
                obra.titulo.lowercase().contains(lowerCaseQuery) ||
                        obra.autor.lowercase().contains(lowerCaseQuery) ||
                        obra.data.lowercase().contains(lowerCaseQuery) ||
                        obra.tema.lowercase().contains(lowerCaseQuery) ||
                        (obra.isAdminAdded && obra.titulo.lowercase()
                            .contains(lowerCaseQuery)) // Inclui obras do admin
            }
        }
        notifyDataSetChanged()
    }
}
