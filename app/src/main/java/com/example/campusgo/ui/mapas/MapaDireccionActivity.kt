package com.example.campusgo.ui.mapas

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityMapaDireccionBinding
import com.example.campusgo.ui.compra.ComprarActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.util.Date

class MapaDireccionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaDireccionBinding
    lateinit var map: MapView
    private lateinit var posicion: GeoPoint
    private var marcador: Marker? = null
    private lateinit var direccion: String

    private lateinit var geocoder: Geocoder

    // Sensor de luz para cambiar el estilo del mapa
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightEventListener: SensorEventListener

    // Radio de la Tierra usado para cálculos de distancia
    val RADIUS_OF_EARTH_KM = 6378
    val bogota = GeoPoint(4.62, -74.07)

    override fun onResume() {
        super.onResume()
        map.onResume()
        map.
        controller.setZoom(18.0)
        map.
        controller.animateTo(bogota)
        // Cambia los colores del mapa si el modo noche está activo
        val uims = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uims.nightMode == UiModeManager.MODE_NIGHT_YES){
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }

        // Registra el sensor de luz
        sensorManager.registerListener(lightEventListener, lightSensor,
            SensorManager.SENSOR_DELAY_FASTEST)

    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        // Detiene el sensor de luz
        sensorManager.unregisterListener(lightEventListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaDireccionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa sensores de luz
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        lightEventListener = createLightSensorListener()

        // Carga configuración del mapa
        Configuration.getInstance().load(this,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        map = binding.osmap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        geocoder = Geocoder(baseContext)

        // Evento para buscar una dirección ingresada por el usuario
        binding.search.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE ||
                i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_SEND) {
                direccion = binding.search.
                text.toString()
                val location = findLocation(direccion)
                if (location != null) {
                    posicion = GeoPoint(location.latitude, location.longitude)
                    addMarker(posicion, direccion)
                    map.controller.animateTo(posicion)
                } else {
                    Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                }
            }
            true
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
    /**
     * Crea el listener para el sensor de luz.
     * Cambia el color del mapa dependiendo de la iluminación ambiental.
     * @return SensorEventListener
     */
    fun createLightSensorListener() : SensorEventListener{
        val ret : SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if(::map.isInitialized){
                    if (event != null) {
                        if(event.values[0] < 5000){
                            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                            map.invalidate()
                        }else {
                            map.overlayManager.tilesOverlay.setColorFilter(null)
                            map.invalidate()
                        }
                    }
                }
            }
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }
        }
        return ret
    }

    /**
     * Calcula la distancia entre dos coordenadas geográficas en kilómetros.
     * @param lat1 Latitud del primer punto
     * @param long1 Longitud del primer punto
     * @param lat2 Latitud del segundo punto
     * @param long2 Longitud del segundo punto
     * @return Distancia en kilómetros
     */
    fun distance(lat1 : Double, long1: Double, lat2:Double, long2:Double) : Double{
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)+ 	Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 	Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return Math.round(result*100.0)/100.0
    }

    /**
     * Busca una ubicación LatLng a partir de una dirección en texto.
     * @param address Dirección como texto
     * @return LatLng correspondiente
     */
    fun findLocation(address : String):LatLng?{
        val addresses = geocoder.getFromLocationName(address, 2)
        if(addresses != null && !addresses.isEmpty()){
            val addr = addresses.get(0)
            val location = LatLng(addr.
            latitude, addr.
            longitude)
            return location
        }
        return null
    }

    /**
     * Agrega un marcador al mapa.
     * @param p GeoPoint de la posición
     * @param snippet Descripción corta
     */
    fun addMarker(p: GeoPoint, snippet: String) {

        if (marcador != null) {
            map.overlays.remove(marcador)
        }

        marcador = createMarker(
            p, snippet, "Punto de encuentro",
            R.drawable.baseline_add_location_alt_24
        )
        if (marcador != null) {
            map.overlays.add(marcador)
            map.invalidate()
        }
    }
    /**
     * Crea un objeto Marker personalizado.
     * @param p GeoPoint de la posición
     * @param title Título del marcador
     * @param desc Descripción del marcador
     * @param iconID ID del recurso del icono
     * @return Marker creado
     */
    fun createMarker(p:GeoPoint, title: String, desc: String, iconID : Int) : Marker? {
        var marker : Marker? = null;
        if(map!=null) {
            marker = Marker(map);
            if (title != null) marker.setTitle(title);
            if (desc != null) marker.setSubDescription(desc);
            if (iconID != 0) {
                val myIcon = getResources().getDrawable(iconID, this.getTheme());
                marker.setIcon(myIcon);
            }
            marker.setPosition(p);
            marker.setAnchor(Marker.
            ANCHOR_CENTER, Marker.
            ANCHOR_BOTTOM);
        }
        return marker
    }

}