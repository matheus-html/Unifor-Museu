package com.example.app_museu

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_museu.databinding.ActivityTelaAudioObraBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log

class Tela_AudioObra : AppCompatActivity(), OnInitListener {
    private lateinit var binding: ActivityTelaAudioObraBinding
    private lateinit var tts: TextToSpeech
    private var isPlaying = false
    private var currentPosition = 0
    private lateinit var listaObras: List<Obra>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaAudioObraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titulo = intent.getStringExtra("obra_titulo")
        val autor = intent.getStringExtra("obra_autor")
        val data = intent.getStringExtra("obra_data")
        val tema = intent.getStringExtra("obra_tema")
        val descricao = intent.getStringExtra("obra_descricao")
        val cover = intent.getStringExtra("obra_cover")

        binding.obraTitulo.text = titulo
        binding.obraAutor.text = autor
        binding.obraData.text = data
        binding.obraTema.text = tema
        binding.obraDescricao.text = descricao

        if (!cover.isNullOrEmpty()) {
            val decodedString = Base64.decode(cover, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            binding.obraImage.setImageBitmap(decodedBitmap)
        }

        tts = TextToSpeech(this, this)

        if (this::listaObras.isInitialized) {
            loadObraData(currentPosition)
        } else {
            loadObrasFromFirebase()
        }

        currentPosition = intent.getIntExtra("obra_position", 0)
        binding.play.setOnClickListener { togglePlay() }
        binding.back.setOnClickListener { obraAnterior() }
        binding.forward.setOnClickListener { proximaObra() }

        binding.toggleButton3.setOnClickListener {
            val obraAtual = listaObras[currentPosition]
            toggleFavorite(obraAtual, !obraAtual.ehFavorito)
        }

    }

    private fun toggleFavorite(obra: Obra, isFavorito: Boolean) {
        val context = binding.root.context
        val obra = listaObras[currentPosition]
        val novoEstadoFavorito = !obra.ehFavorito
        obra.ehFavorito = isFavorito

        // Atualiza o estado no Firestore
        updateFavoritoNoFirestore(obra)

        if (isFavorito) {
            ObrasRepository.addToFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} adicionada aos favoritos", Toast.LENGTH_SHORT).show()
        } else {
            ObrasRepository.removeFromFavoritos(obra.titulo)
            Toast.makeText(context, "${obra.titulo} removida dos favoritos", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadObrasFromFirebase() {
        db.collection("obras")
            .get()
            .addOnSuccessListener { result ->
                listaObras = result.documents.map { doc ->
                    Obra(
                        titulo = doc.getString("titulo") ?: "",
                        autor = doc.getString("autor") ?: "",
                        data = doc.getString("data") ?: "",
                        tema = doc.getString("tema") ?: "",
                        descricao = doc.getString("descricao") ?: "",
                        cover = doc.getString("cover") ?: "",
                        ehFavorito = doc.getBoolean("ehFavorito") ?: false
                    )
                }

                if (listaObras.isNotEmpty()) {
                    if (currentPosition >= listaObras.size) {
                        currentPosition = listaObras.size - 1
                    }
                    loadObraData(currentPosition)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar dados do Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadObraData(position: Int) {
        if (position >= 0 && position < listaObras.size) {
            val obra = listaObras[position]
            binding.obraTitulo.text = obra.titulo
            binding.obraAutor.text = obra.autor
            binding.obraData.text = obra.data
            binding.obraTema.text = obra.tema
            binding.obraDescricao.text = obra.descricao

            if (!obra.cover.isNullOrEmpty()) {
                val decodedString = Base64.decode(obra.cover, Base64.DEFAULT)
                val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                binding.obraImage.setImageBitmap(decodedBitmap)
            } else {

                binding.obraImage.setImageResource(R.drawable.default_image)
            }

            binding.toggleButton3.isChecked = obra.ehFavorito
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


    private fun togglePlay() {
        if (isPlaying) {
            pauseSpeech()
        } else {
            playDescription()
        }
    }

    private fun playDescription() {
        val description = binding.obraDescricao.text.toString()
        tts.speak(description, TextToSpeech.QUEUE_FLUSH, null, null)
        isPlaying = true
        binding.play.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun pauseSpeech() {
        tts.stop()
        isPlaying = false
        binding.play.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun obraAnterior() {
        if (currentPosition > 0) {
            if (isPlaying) {
                pauseSpeech()
            }
            currentPosition--
            loadObraData(currentPosition)
        }
    }

    private fun proximaObra() {
        if (currentPosition < listaObras.size - 1) {
            if (isPlaying) {
                pauseSpeech()
            }
            currentPosition++
            loadObraData(currentPosition)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("pt", "BR")
        } else {
            Toast.makeText(this, "Erro ao inicializar o TTS", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
