package com.example.campusgo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusgo.databinding.ActivityCalificarBinding

class CalificarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificarBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}