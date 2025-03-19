package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusgo.databinding.ActivityNfcactivityBinding

class NFCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNfcactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityNfcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnstar.setOnClickListener{
            startActivity(Intent(baseContext,CalificarActivity::class.java))
        }
    }
}