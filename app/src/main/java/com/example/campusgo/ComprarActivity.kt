package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusgo.databinding.ActivityComprarBinding
import com.example.campusgo.databinding.ActivityMapaCompradorBinding

class ComprarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityComprarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btncompra.setOnClickListener {

            startActivity(Intent(baseContext,MapaCompradorActivity::class.java))

        }

    }
}