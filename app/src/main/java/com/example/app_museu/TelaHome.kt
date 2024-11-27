package com.example.app_museu

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.app_museu.databinding.ActivityTelaHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class TelaHome : AppCompatActivity() {

    private lateinit var binding: ActivityTelaHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTelaHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.main
        navigationView = binding.navigationHome
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout.closeDrawer(navigationView)

        fb = FirebaseFirestore.getInstance()

        checkUserAndUpdateHeader()

        binding.openDrawerButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.openDrawer(navigationView)
            }
        }

        val closeButton: Button = navigationView.getHeaderView(0).findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_configApp -> {
                    replaceFragment(configAPP())
                    true
                }
                R.id.nav_configConta -> {
                    replaceFragment(configConta())
                    true
                }
                R.id.nav_termosPrivacidade -> {
                    replaceFragment(termosPrivacidade())
                    true
                }
                R.id.nav_logout -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Home -> replaceFragment(Home())
                R.id.Audio -> replaceFragment(AudioGuia())
                R.id.Pesquisa -> replaceFragment(Pesquisa())
                R.id.Favoritos -> replaceFragment(FavoritosFragment())
                R.id.Livro -> replaceFragment(LivroInfo())
                else -> false
            }
            true
        }

        replaceFragment(Home())
    }

    private fun checkUserAndUpdateHeader() {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val userDocumentId = sharedPreferences.getString("UserId", null)

        if (userDocumentId != null) {
            fb.collection("contas").document(userDocumentId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("nome") ?: "Nome"
                        val username = document.getString("nomeUsuario") ?: "Nome de Usuário"
                        val profileImageBase64 = document.getString("fotoDePerfil")

                        updateNavHeader(name, username)

                        profileImageBase64?.let {
                            try {
                                val decodedImage = decodeBase64ToImage(it)
                                updateNavHeaderImage(decodedImage)
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(this, "Erro ao decodificar a imagem.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao acessar o Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun decodeBase64ToImage(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    fun updateNavHeader(name: String, username: String) {
        val headerView = navigationView.getHeaderView(0)
        val nameTextView: TextView = headerView.findViewById(R.id.nome)
        val usernameTextView: TextView = headerView.findViewById(R.id.nome_usuario)

        nameTextView.text = name
        usernameTextView.text = username
    }

    fun updateNavHeaderImage(bitmap: Bitmap) {
        val headerView = navigationView.getHeaderView(0)
        val imageView: ImageView? = headerView.findViewById(R.id.foto_de_perfil)

        if (imageView != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            // Tratar o caso onde o ImageView não é encontrado
            Toast.makeText(this, "Erro: ImageView não encontrado", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
