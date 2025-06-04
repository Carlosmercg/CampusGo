package com.example.campusgo.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VendedorLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val uid = auth.currentUser?.uid ?: return

                // Buscar pedidos activos donde vendedorID == UID
                db.collection("Pedidos")
                    .whereEqualTo("vendedorID", uid)
                    .whereEqualTo("estado", "activo")
                    .get()
                    .addOnSuccessListener { documentos ->
                        for (doc in documentos) {
                            val pedidoId = doc.id
                            db.collection("Pedidos").document(pedidoId)
                                .update(
                                    "latvendedor", location.latitude,
                                    "longvendedor", location.longitude
                                )
                        }
                    }
                    .addOnFailureListener {
                        Log.e("LocationService", "Error consultando pedidos: ${it.message}")
                    }
            }
        }

        startLocationUpdates()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10_000 // 10 segundos
            fastestInterval = 5_000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
