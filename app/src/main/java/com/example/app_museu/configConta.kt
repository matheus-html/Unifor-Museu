package com.example.app_museu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class configConta : Fragment() {

    private lateinit var editName: EditText
    private lateinit var editUsername: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonEditIcon: Button
    private lateinit var imageProfile: ImageView
    private lateinit var fb: FirebaseFirestore

    private val PICK_IMAGE = 1
    private var imageUri: Uri? = null
    private var imageBase64: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_configconta, container, false)
        fb = FirebaseFirestore.getInstance()
        initializeViews(view)
        loadAccountSettings()
        buttonSave.setOnClickListener { saveAccountSettings() }
        buttonEditIcon.setOnClickListener { openGallery() }
        return view
    }

    private fun initializeViews(view: View) {
        editName = view.findViewById(R.id.edit_name)
        editUsername = view.findViewById(R.id.edit_username)
        buttonSave = view.findViewById(R.id.button_save_account)
        buttonEditIcon = view.findViewById(R.id.button_edit_icon)
        imageProfile = view.findViewById(R.id.image_profile)
    }

    private fun loadAccountSettings() {
        val sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val userDocumentId = sharedPreferences.getString("UserId", null)

        if (userDocumentId != null) {
            fb.collection("contas").document(userDocumentId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("nome") ?: ""
                        val username = document.getString("nomeUsuario") ?: ""
                        val profileImageUrl = document.getString("fotoDePerfil")

                        editName.setText(name)
                        editUsername.setText(username)

                        // Se a imagem estiver disponível em base64, decodifique e mostre
                        profileImageUrl?.let {
                            val decodedImage = decodeBase64ToImage(it)
                            imageProfile.setImageBitmap(decodedImage)
                        }

                        Log.d("ConfigConta", "Nome: $name, Nome de Usuário: $username")
                    } else {
                        Toast.makeText(requireContext(), "Conta não encontrada.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Erro ao carregar os dados: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ConfigConta", "Erro ao carregar dados: ${exception.message}")
                }
        } else {
            Toast.makeText(requireContext(), "Erro: ID do usuário não encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAccountSettings() {
        val name = editName.text.toString()
        val username = editUsername.text.toString()


        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

/*        val sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("Name", name)
            putString("Username", username)
            putString("ProfileImage", imageBase64)
            apply()
        }*/

        val sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("Name", name)
            putString("Username", username)
            apply()
        }

        updateNavHeader(name, username)

        val userDocumentId = sharedPreferences.getString("UserId", null)

        if (userDocumentId != null) {
            // Salvar nome de usuário e nome no Firebase
            val userRef = fb.collection("contas").document(userDocumentId)

            // Atualiza nome e nome de usuário, com ou sem foto
            val updates = hashMapOf<String, Any>(
                "nome" to name,
                "nomeUsuario" to username
            )

            // Se a foto foi alterada, atualiza a foto também
            imageBase64?.let {
                updates["fotoDePerfil"] = it
            }

            userRef.update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Erro ao salvar configurações: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateNavHeader(name: String, username: String) {
        val activity = requireActivity() as? TelaHome
        activity?.updateNavHeader(name, username)

        // Se a imagem foi alterada, atualize a imagem no NavHeader
        imageBase64?.let {
            val decodedImage = decodeBase64ToImage(it)
            activity?.updateNavHeaderImage(decodedImage)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageProfile.setImageURI(imageUri)
            convertImageToBase64()
        }
    }

    private fun convertImageToBase64() {
        val drawable = imageProfile.drawable
        if (drawable != null && drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }
    }

    private fun decodeBase64ToImage(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

}
