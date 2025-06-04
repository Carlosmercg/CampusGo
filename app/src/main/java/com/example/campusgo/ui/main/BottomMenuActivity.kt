package com.example.campusgo.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.campusgo.R
import com.example.campusgo.services.VendedorLocationService
import com.example.campusgo.ui.compra.CarritoActivity
import com.example.campusgo.ui.chat.ChatsListActivity
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BottomMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verificarPermisosYIniciarServicio()
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

    private fun verificarPermisosYIniciarServicio() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
        } else {
            iniciarServicioUbicacion()
        }
    }

    private fun iniciarServicioUbicacion() {
        val intent = Intent(this, VendedorLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            iniciarServicioUbicacion()
        }
    }
}
