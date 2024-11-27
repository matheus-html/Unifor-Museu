package com.example.app_museu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app_museu.databinding.CardCellBinding
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

class ObraAdapter(
    private var obras: List<Obra>,
    private val clickListener: ObraClickListener
) : RecyclerView.Adapter<CardViewHolder>() {

    companion object {
        const val TIPO_PADRAO = 0
        const val TIPO_ADMIN = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellBinding.inflate(from, parent, false)

        return CardViewHolder(binding, clickListener)
    }

    override fun getItemViewType(position: Int): Int {
        return if (obras[position].isAdminAdded) {
            TIPO_ADMIN
        } else {
            TIPO_PADRAO
        }
    }

    override fun getItemCount(): Int = obras.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bindObra(obras[position])
    }

    fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
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
