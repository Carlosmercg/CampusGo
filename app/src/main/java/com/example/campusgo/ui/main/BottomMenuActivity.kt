package com.example.campusgo.ui.main

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusgo.R
import com.example.campusgo.ui.compra.CarritoActivity
import com.example.campusgo.ui.chat.ChatsListActivity
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.usuario.PerfilActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.TilesOverlay

open class BottomMenuActivity : AppCompatActivity() {

    private lateinit var posicion: GeoPoint
    private var lastSavedLocation: GeoPoint? = null

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val RADIUS_OF_EARTH_KM = 6378

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallback()
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

    override fun onResume() {
        super.onResume()
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    /**
     * Callback para cuando se activa la ubicación del dispositivo (GPS).
     */
    private val locationSettings = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ActivityResultCallback {
            if(it.resultCode == RESULT_OK){
                startLocationUpdates()
            }
        }
    )

    /**
     * Callback para el permiso de ubicación.
     */
    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback {
            if(it){
                locationSettings()
            }
        }
    )

    private fun locationSettings(){
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here. // ...
            startLocationUpdates()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val isr : IntentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettings.launch(isr)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.i("error","There is no GPS hardware")
                }
            }
        }
    }

    private fun createLocationRequest(): com.google.android.gms.location.LocationRequest {
        val request = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()
        return request
    }

    private fun createLocationCallback(): LocationCallback {
        val callback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val loc = result.lastLocation
                if(loc != null){
                    val newPosition = GeoPoint(loc.latitude, loc.longitude)
                    posicion = newPosition
                    posicion = GeoPoint(loc.latitude, loc.longitude)
                    updateUI(loc)

                    if (lastSavedLocation == null ||
                        distance(lastSavedLocation!!.latitude, lastSavedLocation!!.longitude,
                            newPosition.latitude, newPosition.longitude) > 0.03
                    ) {
                        val location = result.lastLocation ?: return
                        val uid = auth.currentUser?.uid ?: return

                        db.collection("Pedidos")
                            .whereEqualTo("vendedorId", uid)
                            .whereEqualTo("estado", "aceptado")
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
            }
        }
        return callback
    }

    private fun updateUI(location : Location){
        Log.i("GPS_APP", "(lat: ${location.latitude}, long: ${location.longitude})")
        posicion = GeoPoint(location.latitude, location.longitude)
    }
    /**
     * Inicia la recepción de actualizaciones de ubicación.
     */
    private fun startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    /**
     * Detiene la recepción de actualizaciones de ubicación.
     */
    private fun stopLocationUpdates(){
        locationClient.removeLocationUpdates(locationCallback)
    }

    private fun distance(lat1 : Double, long1: Double, lat2:Double, long2:Double) : Double{
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)+ 	Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 	Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return Math.round(result*100.0)/100.0
    }

}
