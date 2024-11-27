package com.example.app_museu

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import android.util.Base64

class tela_adicionar_obra : AppCompatActivity() {
    private lateinit var editTextTitulo: EditText
    private lateinit var editTextAutor: EditText
    private lateinit var editTextData: EditText
    private lateinit var editTextTema: EditText
    private lateinit var editTextDescricao: EditText
    private lateinit var buttonAddImage: Button
    private lateinit var buttonSalvar: Button
    private lateinit var imageView: ImageView
    private var obraImageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private var obraImageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_obra)

        editTextTitulo = findViewById(R.id.editTextTitulo)
        editTextAutor = findViewById(R.id.editTextAutor)
        editTextData = findViewById(R.id.editTextData)
        editTextTema = findViewById(R.id.editTextTema)
        editTextDescricao = findViewById(R.id.editTextDescricao)
        buttonAddImage = findViewById(R.id.buttonAddImage)
        buttonSalvar = findViewById(R.id.buttonSalvarEdit)
        imageView = findViewById(R.id.imageViewPreview)

        buttonAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(intent)
        }

        buttonSalvar.setOnClickListener {
            val titulo = editTextTitulo.text.toString()
            val autor = editTextAutor.text.toString()
            val data = editTextData.text.toString()
            val tema = editTextTema.text.toString()
            val descricao = editTextDescricao.text.toString()

            if (titulo.isNotEmpty() && autor.isNotEmpty() && data.isNotEmpty() && tema.isNotEmpty() && descricao.isNotEmpty()) {
                if (obraImageBase64 == null) {
                    Toast.makeText(this, "Selecione uma imagem antes de salvar.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val novaObra = Obra(
                    isAdminAdded = true,
                    autor = autor,
                    titulo = titulo,
                    data = data,
                    tema = tema,
                    descricao = descricao,
                    cover = obraImageBase64!!
                )

                salvarObraNoFirestore(novaObra)

                AlertDialog.Builder(this)
                    .setTitle("Obra Salva")
                    .setMessage("A obra foi salva com sucesso! Você deseja voltar para a Tela Home?")
                    .setPositiveButton("Sim") { _, _ ->
                        val intent = Intent(this, TelaHome::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton("Não") { _, _ ->
                        val intent = Intent(this, tela_admin_inicial::class.java)
                        startActivity(intent)
                    }
                    .show()
            } else {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
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

    private fun convertImageToBase64() {
        val drawable = imageView.drawable
        if (drawable != null && drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            obraImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } else {
            obraImageBase64 = null
        }
    }

    private fun salvarObraNoFirestore(obra: Obra) {
        val obraRef = db.collection("obras").document()

        obraRef.set(obra)
            .addOnSuccessListener {
                Toast.makeText(this, "Obra salva no Firestore com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar obra: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable && this.bitmap != null) {
            return this.bitmap
        }

        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}
