package com.example.app_museu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.example.app_museu.databinding.ActivityTelaSaibaMaisBinding

class Tela_saiba_mais : AppCompatActivity() {

    private lateinit var binding: ActivityTelaSaibaMaisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTelaSaibaMaisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("AppMuseuPrefs", MODE_PRIVATE)

        val titulo = sharedPref.getString("obra_titulo", "Título não disponível")
        val autor = sharedPref.getString("obra_autor", "Autor desconhecido")
        val data = sharedPref.getString("obra_data", "Data desconhecida")
        val tema = sharedPref.getString("obra_tema", "Tema não informado")
        val descricao = sharedPref.getString("obra_descricao", "Descrição não disponível")
        val coverBase64 = sharedPref.getString("obra_cover", null)

        val bitmap = decodeBase64ToBitmap(coverBase64)

        if (bitmap != null) {
            binding.cover.setImageBitmap(bitmap)
        } else {
            binding.cover.setImageResource(R.drawable.default_image)
        }

        binding.titulo.text = titulo
        binding.autor.text = autor
        binding.data.text = data
        binding.tema.text = tema
        binding.obraDescricao.text = descricao
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
