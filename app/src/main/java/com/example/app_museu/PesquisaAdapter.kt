package com.example.app_museu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding

class PesquisaAdapter(
    private var obras: List<Obra>,
    private val clickListener: ObraClickListener
) : RecyclerView.Adapter<CardViewHolder>() {

    // Inicialmente, a lista filtrada está vazia
    private var obrasFiltradas: List<Obra> = obras

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellBinding.inflate(from, parent, false)
        return CardViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int = obrasFiltradas.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bindObra(obrasFiltradas[position])
    }

    // Método para filtrar as obras
    fun filter(query: String) {
        obrasFiltradas = if (query.isEmpty()) {
            obras // Se não há consulta, exibe todas as obras
        } else {
            val lowerCaseQuery = query.lowercase()
            obras.filter { obra ->
                obra.titulo.lowercase().contains(lowerCaseQuery) ||
                        obra.autor.lowercase().contains(lowerCaseQuery) ||
                        obra.data.lowercase().contains(lowerCaseQuery) ||
                        obra.tema.lowercase().contains(lowerCaseQuery) ||
                        (obra.isAdminAdded && obra.titulo.lowercase().contains(lowerCaseQuery)) // Inclui obras do admin
            }
        }
        notifyDataSetChanged()
    }

    // Método para atualizar a lista de obras no adapter
    fun updateList(newObras: List<Obra>) {
        obras = newObras
        obrasFiltradas = newObras // Atualiza a lista filtrada também
        notifyDataSetChanged() // Notifica o adapter sobre a atualização
    }
}
