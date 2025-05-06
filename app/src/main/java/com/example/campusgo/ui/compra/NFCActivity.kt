package com.example.campusgo.ui.compra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityNfcactivityBinding

class NFCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNfcactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityNfcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnstar.setOnClickListener{
            startActivity(Intent(baseContext, CalificarActivityVendedor::class.java))
        }
    }
}