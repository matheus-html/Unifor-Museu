package com.example.app_museu

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton

class LivroInfo : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_livro_info, container, false)

        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)
        val buttonHistoria = view.findViewById<Button>(R.id.button_historia)
        val buttonInfo = view.findViewById<Button>(R.id.buttonInfo)
        val buttonMissao = view.findViewById<Button>(R.id.button_missao)

        buttonHistoria.setOnClickListener {
            val fragment = HistoriaAppMuseu()
            (activity as TelaHome).replaceFragment(fragment)
        }

        imageButton.setOnClickListener {
            val intent = Intent(activity, WebViewActivity::class.java)
            startActivity(intent)
        }

        buttonMissao.setOnClickListener {
            val fragment = MissaoMuseu()
            (activity as TelaHome).replaceFragment(fragment)
        }

        buttonInfo.setOnClickListener {
            val fragment = MaisInfoMuseu()
            (activity as TelaHome).replaceFragment(fragment)
        }
        return view
    }
}
