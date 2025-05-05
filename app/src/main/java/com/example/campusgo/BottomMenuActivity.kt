package com.example.campusgo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.R
import com.example.campusgo.compra.CarritoActivity
import com.example.campusgo.chat.ChatsActivity
import com.example.campusgo.usuario.PerfilActivity
import com.example.campusgo.Ingresar.HomeActivity
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
                        overridePendingTransition(0, 0) // Elimina la animación
                        finish()
                    }
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
                        Log.d("ChatsActivityTest", "Se intenta abrir ChatsActivity")
                        startActivity(Intent(this, ChatsActivity::class.java))
                        overridePendingTransition(0, 0)
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
