package com.example.campusgo.ui.compra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityCalificarBinding

class CalificarActivityVendedor : AppCompatActivity() {

    private lateinit var binding: ActivityCalificarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificarBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}