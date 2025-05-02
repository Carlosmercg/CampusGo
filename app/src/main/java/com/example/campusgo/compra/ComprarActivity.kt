package com.example.campusgo.compra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityComprarBinding
import com.example.campusgo.models.MapaCompradorActivity

class ComprarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityComprarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btncompra.setOnClickListener {

            startActivity(Intent(baseContext, MapaCompradorActivity::class.java))

        }

    }
}