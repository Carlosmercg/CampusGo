package com.example.campusgo.ui.mapas

import android.app.UiModeManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityMapaDireccionBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.util.*

class MapaDireccionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaDireccionBinding
    lateinit var map: MapView
    private lateinit var posicion: GeoPoint
    private var marcador: Marker? = null
    private lateinit var direccion: String

    private lateinit var geocoder: Geocoder

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightEventListener: SensorEventListener

    val RADIUS_OF_EARTH_KM = 6378
    val bogota = GeoPoint(4.62, -74.07)

    // Permiso micrófono
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para reconocimiento de voz
    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val resultList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoReconocido = resultList?.get(0) ?: return@registerForActivityResult
            binding.search.setText(textoReconocido)

            // Ejecutar búsqueda automáticamente
            direccion = textoReconocido
            val location = findLocation(direccion)
            if (location != null) {
                posicion = location
                addMarker(posicion, direccion)
                map.controller.animateTo(posicion)
            } else {
                Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        map.controller.setZoom(18.0)
        map.controller.animateTo(bogota)

        val uims = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uims.nightMode == UiModeManager.MODE_NIGHT_YES) {
            binding.search.setTextColor(Color.WHITE)
            binding.search.post {
                binding.search.setTextColor(Color.WHITE)
                binding.search.setHintTextColor(Color.LTGRAY)
            }
            binding.btnMic.post {
                binding.btnMic.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }

        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        sensorManager.unregisterListener(lightEventListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaDireccionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        lightEventListener = createLightSensorListener()

        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        map = binding.osmap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        geocoder = Geocoder(baseContext)

        binding.search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {

                direccion = binding.search.text.toString()
                val location = findLocation(direccion)
                if (location != null) {
                    posicion = location
                    addMarker(posicion, direccion)
                    map.controller.animateTo(posicion)
                } else {
                    Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        binding.btnMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.button.setOnClickListener {
            if (!::direccion.isInitialized || direccion.isBlank()) {
                Toast.makeText(this, "Por favor ingrese una dirección antes de continuar", Toast.LENGTH_SHORT).show()
            } else {
                val resultIntent = Intent()
                resultIntent.putExtra("direccion", direccion)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Di una dirección")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Tu dispositivo no soporta reconocimiento por voz", Toast.LENGTH_SHORT).show()
        }
    }

    fun createLightSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (::map.isInitialized && event != null) {
                    if (event.values[0] < 5000) {
                        map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    } else {
                        map.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                    map.invalidate()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return Math.round(result * 100.0) / 100.0
    }

    fun findLocation(address: String): GeoPoint? {
        val addresses = geocoder.getFromLocationName(address, 2)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            return GeoPoint(addr.latitude, addr.longitude)
        }
        return null
    }

    fun addMarker(p: GeoPoint, snippet: String) {
        if (marcador != null) {
            map.overlays.remove(marcador)
        }
        marcador = createMarker(p, snippet, "Punto de encuentro", R.drawable.baseline_add_location_alt_24)
        marcador?.let {
            map.overlays.add(it)
            map.invalidate()
        }
    }

    fun createMarker(p: GeoPoint, title: String, desc: String, iconID: Int): Marker {
        val marker = Marker(map)
        marker.title = title
        marker.subDescription = desc
        val myIcon = resources.getDrawable(iconID, theme)
        marker.icon = myIcon
        marker.position = p
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        return marker
    }
}
