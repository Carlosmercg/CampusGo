package com.example.campusgo.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.R
import com.example.campusgo.ui.compra.CarritoActivity
import com.example.campusgo.ui.chat.ChatsListActivity
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BottomMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setupBottomNavigation(bottomNav: BottomNavigationView, currentActivityId: Int) {
        bottomNav.selectedItemId = currentActivityId

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentActivityId != R.id.nav_home) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else recreate()
                    true
                }
                R.id.nav_carrito -> {
                    if (currentActivityId != R.id.nav_carrito) {
                        startActivity(Intent(this, CarritoActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_chats -> {
                    if (currentActivityId != R.id.nav_chats) {
                        startActivity(Intent(this, ChatsListActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_cuenta -> {
                    if (currentActivityId != R.id.nav_cuenta) {
                        startActivity(Intent(this, PerfilActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
