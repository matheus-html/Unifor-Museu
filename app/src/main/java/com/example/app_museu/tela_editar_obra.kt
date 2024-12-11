package com.example.app_museu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import java.io.ByteArrayOutputStream

class TelaEditarObra : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editTextTitulo: EditText
    private lateinit var editTextAutor: EditText
    private lateinit var editTextData: EditText
    private lateinit var editTextTema: EditText
    private lateinit var editTextDescricao: EditText
    private lateinit var buttonSalvarEdit: Button
    private lateinit var buttonAddImage: Button
    private var obraImageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private var obraImageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_editar_obra)

        editTextTitulo = findViewById(R.id.editTextTitulo)
        editTextAutor = findViewById(R.id.editTextAutor)
        editTextData = findViewById(R.id.editTextData)
        editTextTema = findViewById(R.id.editTextTema)
        editTextDescricao = findViewById(R.id.editTextDescricao)
        buttonSalvarEdit = findViewById(R.id.buttonSalvarEdit)
        buttonAddImage = findViewById(R.id.buttonAddImage)
        imageView = findViewById(R.id.imageView5)

        val titulo = intent.getStringExtra("titulo_obra") ?: return
        carregarDadosObra(titulo)

        buttonAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(intent)
        }

        buttonSalvarEdit.setOnClickListener {
            val updatedObra = Obra(
                autor = editTextAutor.text.toString(),
                titulo = editTextTitulo.text.toString(),
                data = editTextData.text.toString(),
                tema = editTextTema.text.toString(),
                descricao = editTextDescricao.text.toString(),
                cover = obraImageBase64 ?: ""
            )
            updateObraInFirestore(updatedObra, titulo)

            val titulo = editTextTitulo.text.toString()
            if (titulo.isNotBlank()) {
                updateObraInFirestore(updatedObra, titulo)
            } else {
                Toast.makeText(this, "O título não pode ser vazio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                obraImageUri = data?.data
                obraImageUri?.let {
                    imageView.setImageURI(it)
                    convertImageToBase64()
                }
            }
        }


    private fun carregarDadosObra(titulo: String) {
        db.collection("obras").whereEqualTo("titulo", titulo).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val obra = document.toObject(Obra::class.java)
                    obra?.let {
                        editTextTitulo.setText(it.titulo)
                        editTextAutor.setText(it.autor)
                        editTextData.setText(it.data)
                        editTextTema.setText(it.tema)
                        editTextDescricao.setText(it.descricao)

                        it.cover?.let { base64Image ->
                            mostrarImagemBase64(base64Image)
                        }
                    }

                } else {
                    Toast.makeText(this, "Obra não encontrada", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarImagemBase64(base64Image: String) {
        try {
            val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao exibir a imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeImage(imageUri: Uri): Bitmap? {
        val contentResolver = applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        val maxWidth = 800
        val maxHeight = 800

        val width = originalBitmap.width
        val height = originalBitmap.height

        val aspectRatio = width.toFloat() / height.toFloat()
        var newWidth = width
        var newHeight = height

        if (width > height) {
            if (width > maxWidth) {
                newWidth = maxWidth
                newHeight = (newWidth / aspectRatio).toInt()
            }
        } else {
            if (height > maxHeight) {
                newHeight = maxHeight
                newWidth = (newHeight * aspectRatio).toInt()
            }
        }

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    }

    private fun convertImageToBase64() {
        if (obraImageUri != null) {
            val resizedBitmap = resizeImage(obraImageUri!!)
            if (resizedBitmap != null) {
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream) // Compress the image further
                val imageBytes = outputStream.toByteArray()
                obraImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            }
        } else {
            obraImageBase64 = null
        }
    }

    private fun updateObraInFirestore(obra: Obra, titulo: String) {
        db.collection("obras").whereEqualTo("titulo", titulo).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    db.collection("obras").document(documentId)
                        .set(obra)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Obra atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao atualizar obra: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
}
