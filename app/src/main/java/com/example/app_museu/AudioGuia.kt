package com.example.app_museu

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_museu.databinding.FragmentAudioGuiaBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineStart
import java.io.File
import java.io.FileOutputStream
import android.util.Base64
import android.util.Log

class AudioGuia : Fragment(), ObraClickListener {

    private lateinit var binding: FragmentAudioGuiaBinding
    private lateinit var db: FirebaseFirestore
    private val listaObras = mutableListOf<Obra>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioGuiaBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadObrasFromFirebase()
    }

    private fun loadObrasFromFirebase() {
        db.collection("obras")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val obra = document.toObject(Obra::class.java)
                    listaObras.add(obra)
                }

                binding.recyclerViewObrasAUDIOGUIA.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                val adapter = ObraAdapter(listaObras, this)
                binding.recyclerViewObrasAUDIOGUIA.adapter = adapter
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun saveImageToCache(context: Context, base64Image: String): String {
        val fileName = "image_${System.currentTimeMillis()}.png"
        val file = File(context.cacheDir, fileName)

        try {
            val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            val resizedBitmap = Bitmap.createScaledBitmap(decodedBitmap, 500, 500, false)
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    override fun onClick(obra: Obra) {
        val position = listaObras.indexOf(obra)
        val imagePath = saveImageToCache(requireContext(), obra.cover)

        val sharedPreferences = requireContext().getSharedPreferences("obra_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("obra_titulo", obra.titulo)
        editor.putString("obra_autor", obra.autor)
        editor.putString("obra_data", obra.data)
        editor.putString("obra_tema", obra.tema)
        editor.putString("obra_descricao", obra.descricao)
        editor.putString("obra_cover", obra.cover)
        editor.putString("obra_image_path", imagePath)
        editor.apply()

        val intent = Intent(requireContext(), Tela_AudioObra::class.java).apply {
            putExtra("obra_position", position)
            putExtra("obra_key", "obra_data")
        }
        startActivity(intent)
    }

}
