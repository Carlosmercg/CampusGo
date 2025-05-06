package com.example.campusgo.ui.mapas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.ui.chat.ChatActivity
import com.example.campusgo.ui.compra.NFCActivity
import com.example.campusgo.databinding.ActivityMapaCompradorBinding

class MapaCompradorActivity : AppCompatActivity() {

    private lateinit var binding :ActivityMapaCompradorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMapaCompradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

    binding.btnchat.setOnClickListener{
        startActivity(Intent(baseContext, ChatActivity::class.java))
    }

    binding.btnnfc.setOnClickListener{
        startActivity(Intent(baseContext, NFCActivity::class.java))
    }

    }
}