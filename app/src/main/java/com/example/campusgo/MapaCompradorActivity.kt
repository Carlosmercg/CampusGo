package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusgo.databinding.ActivityChatBinding
import com.example.campusgo.databinding.ActivityMapaCompradorBinding
import com.example.campusgo.databinding.ActivityNfcactivityBinding

class MapaCompradorActivity : AppCompatActivity() {

    private lateinit var binding :ActivityMapaCompradorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMapaCompradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

    binding.btnchat.setOnClickListener{
        startActivity(Intent(baseContext,ChatActivity::class.java))
    }

    binding.btnnfc.setOnClickListener{
        startActivity(Intent(baseContext,NFCActivity::class.java))
    }

    }
}