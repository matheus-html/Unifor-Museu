package com.example.app_museu

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ObrasAdapter(private var obrasList: MutableList<Obra>) :
    RecyclerView.Adapter<ObrasAdapter.ObraViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_obra_admin, parent, false)
        return ObraViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObraViewHolder, position: Int) {
        val obra = obrasList[position]
        holder.titulo.text = obra.titulo
        holder.autor.text = obra.autor
        holder.tema.text = obra.tema
        holder.data.text = obra.data
        holder.descricao.text = obra.descricao

        val bitmap = decodeBase64ToBitmap(obra.cover)
        if (bitmap != null) {
            holder.coverImageView.setImageBitmap(bitmap)
        } else {
            holder.coverImageView.setImageResource(R.drawable.default_image) // Imagem padrão se falhar na decodificação
        }

        holder.buttonViewObra.setOnClickListener {
            val context = holder.itemView.context
            val sharedPreferences = context.getSharedPreferences("AppMuseuPrefs", Context.MODE_PRIVATE)

            sharedPreferences.edit().apply {
                putString("obra_titulo", obra.titulo)
                putString("obra_autor", obra.autor)
                putString("obra_data", obra.data)
                putString("obra_tema", obra.tema)
                putString("obra_descricao", obra.descricao)
                putString("obra_cover", obra.cover) // Adicione a cover para visualização
                apply()
            }

            val intent = Intent(context, Tela_saiba_mais::class.java)
            context.startActivity(intent)
        }

        holder.buttonEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, TelaEditarObra::class.java)
            intent.putExtra("titulo_obra", obra.titulo)
            holder.itemView.context.startActivity(intent)
        }

        holder.buttonDelete.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            db.collection("obras")
                .whereEqualTo("titulo", obra.titulo)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentId = querySnapshot.documents[0].id
                        db.collection("obras").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(holder.itemView.context, "Obra deletada com sucesso!", Toast.LENGTH_SHORT).show()
                                removeItemAt(position)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(holder.itemView.context, "Erro ao deletar obra: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(holder.itemView.context, "Obra não encontrada!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context, "Erro ao buscar obra: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = obrasList.size

    fun updateList(newList: List<Obra>) {
        obrasList.clear()
        obrasList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun removeItemAt(position: Int) {
        obrasList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
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

    class ObraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.coverImageView)
        val buttonViewObra: Button = itemView.findViewById(R.id.buttonViewObra)
        val titulo: TextView = itemView.findViewById(R.id.titulo)
        val autor: TextView = itemView.findViewById(R.id.autor)
        val tema: TextView = itemView.findViewById(R.id.tema)
        val data: TextView = itemView.findViewById(R.id.data)
        val descricao: TextView = itemView.findViewById(R.id.descricao)
        val buttonEdit: Button = itemView.findViewById(R.id.ButtonEditObra)
        val buttonDelete: Button = itemView.findViewById(R.id.ButtonRemoveObra)
    }
}
