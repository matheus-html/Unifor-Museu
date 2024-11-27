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

/*        super.onCreate(savedInstanceState)
        binding = ActivityTelaAudioObraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperando os dados de SharedPreferences
        val sharedPreferences = getSharedPreferences("obra_data", Context.MODE_PRIVATE)
        val titulo = sharedPreferences.getString("obra_titulo", "")
        val autor = sharedPreferences.getString("obra_autor", "")
        val data = sharedPreferences.getString("obra_data", "")
        val tema = sharedPreferences.getString("obra_tema", "")
        val descricao = sharedPreferences.getString("obra_descricao", "")
        val cover = sharedPreferences.getString("obra_cover", "")
        val position = sharedPreferences.getInt("obra_position", -1)
        val imagePath = sharedPreferences.getString("obra_image_path", "")

        // Exibindo os dados na tela
        binding.obraTitulo.text = titulo
        binding.obraAutor.text = autor
        binding.obraData.text = data
        binding.obraTema.text = tema
        binding.obraDescricao.text = descricao

        // Carregando a imagem, se disponível
        if (!imagePath.isNullOrEmpty()) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                binding.obraImage.setImageBitmap(bitmap)
            }
        }

        tts = TextToSpeech(this, this)*/


        // Se já tiver carregado as obras antes, carregue localmente
        if (this::listaObras.isInitialized) {
            loadObraData(currentPosition)
        } else {
            loadObrasFromFirebase()
        }

        currentPosition = intent.getIntExtra("obra_position", 0)
        binding.play.setOnClickListener { togglePlay() }
        binding.back.setOnClickListener { obraAnterior() }
        binding.forward.setOnClickListener { proximaObra() }
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
                        cover = doc.getString("cover") ?: ""
                    )
                }

                if (listaObras.isNotEmpty()) {
                    if (currentPosition >= listaObras.size) {
                        currentPosition = listaObras.size - 1
                    }
                    loadObraData(currentPosition)  // Carrega os dados da obra selecionada
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

            // Verificando se o coverBase64 não é nulo
            if (!obra.cover.isNullOrEmpty()) {
                // Decodificando a string Base64 para Bitmap
                val decodedString = Base64.decode(obra.cover, Base64.DEFAULT)
                val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                // Atualizando a imagem na ImageView
                binding.obraImage.setImageBitmap(decodedBitmap)
            } else {
                // Caso o coverBase64 seja nulo, define uma imagem padrão
                binding.obraImage.setImageResource(R.drawable.default_image)
            }
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
