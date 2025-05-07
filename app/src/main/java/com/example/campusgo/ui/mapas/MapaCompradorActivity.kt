package com.example.campusgo.mapas

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusgo.R
import com.example.campusgo.chat.ChatActivity
import com.example.campusgo.compra.Codigo_NFC
import com.example.campusgo.compra.NFCActivity
import com.example.campusgo.databinding.ActivityMapaCompradorBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.util.Date

class MapaCompradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaCompradorBinding
    lateinit var map: MapView

    private lateinit var posicion: GeoPoint
    private lateinit var posicionVendedor: GeoPoint
    private var ultimaPosicionVendedor: GeoPoint? = null
    private var marcadorVendedor: Marker? = null

    private lateinit var geocoder: Geocoder
    private lateinit var roadManager: RoadManager

    // Radio de la Tierra usado para cálculos de distancia
    val RADIUS_OF_EARTH_KM = 6378

    // Sensor de luz para cambiar el estilo del mapa
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightEventListener: SensorEventListener

    override fun onResume() {
        super.onResume()
        map.onResume()
        lifecycleScope.launch {
            direccionFirestore()
            delay(6000)
            while (true) {
                posicionVendedoresFirestore()
                delay(10000)
                if (::posicion.isInitialized && ::posicionVendedor.isInitialized) {
                        drawRoute(posicion, posicionVendedor)
                        map.controller.setZoom(15.0)
                        map.invalidate()
                }
                delay(50000)
            }
        }
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
        binding= ActivityMapaCompradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carga configuración del mapa
        Configuration.getInstance().load(this,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        map = binding.osmap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        geocoder = Geocoder(baseContext)
        roadManager = OSRMRoadManager(this, "ANDROID")

        // Permite la ejecución de operaciones de red en el hilo principal
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        map.overlays.add(createOverlayEvents())

        // Inicializa sensores de luz
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        lightEventListener = createLightSensorListener()

        binding.btnnfc.setOnClickListener{
            startActivity(Intent(this, Codigo_NFC::class.java))
        }

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
     * Crea el listener para el sensor de luz.
     * Cambia el color del mapa dependiendo de la iluminación ambiental.
     * @return SensorEventListener
     */
    fun createLightSensorListener() : SensorEventListener {
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
     * Agrega un marcador al mapa.
     * @param p GeoPoint de la posición
     * @param snippet Descripción corta
     * @param longPressed Indica si es de un long press
     */
    fun addMarker(p: GeoPoint, snippet: String) {
        var addressText = "Ubicación desconocida"
        val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
        val distancia = distance(posicion.latitude, posicion.longitude, p.latitude, p.longitude)
        Toast.makeText(baseContext, "Distancia: %.2f km".format(distancia), Toast.LENGTH_LONG).show()

        if (addresses != null && addresses.isNotEmpty()) {
            addressText = addresses[0].getAddressLine(0) ?: "Ubicación desconocida"
        }

        val marker = createMarker(
            p, addressText, "",
            R.drawable.baseline_add_location_alt_24
        )
        if (marker != null) {
            map.overlays.add(marker)
            map.invalidate()
        }
    }

    fun markerVendedor(p: GeoPoint, snippet: String) {
        var addressText = "Ubicación desconocida"
        val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
        val distancia = distance(posicion.latitude, posicion.longitude, p.latitude, p.longitude)
        Toast.makeText(baseContext, "Distancia: %.2f km".format(distancia), Toast.LENGTH_LONG).show()

        if (addresses != null && addresses.isNotEmpty()) {
            addressText = addresses[0].getAddressLine(0) ?: "Ubicación desconocida"
        }

         marcadorVendedor = createMarker(
            p, addressText, "",
            R.drawable.baseline_add_location_alt_24
        )
        if (marcadorVendedor != null) {
            map.overlays.add(marcadorVendedor)
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
            marker.setAnchor(
                Marker.
                ANCHOR_CENTER, Marker.
                ANCHOR_BOTTOM);
        }
        return marker
    }

    fun createOverlayEvents() : MapEventsOverlay {
        val overlayEvents = MapEventsOverlay(object : MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }
            override fun longPressHelper(p: GeoPoint?): Boolean {
                return true
            }
        })
        return overlayEvents
    }
    /**
     * Busca una ubicación LatLng a partir de una dirección en texto.
     * @param address Dirección como texto
     * @return LatLng correspondiente
     */
    fun findLocation(address : String): LatLng?{
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
     * Obtiene la dirección a partir de una ubicación (LatLng).
     * @param location LatLng de la posición
     * @return Dirección en formato String
     */
    fun findAddress (location : LatLng):String?{
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2)
        if(addresses != null && !addresses.isEmpty()){
            val addr = addresses.get(0)
            val locname = addr.getAddressLine(0)
            return locname
        }
        return null
    }

    fun direccionFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Pedidos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val direccion = document.getString("direccion")
                    val compradorId = document.getString("compradorID")
                    if (direccion != null) {
                        val latLng = findLocation(direccion)
                        if (latLng != null) {
                            posicion = GeoPoint(latLng.latitude, latLng.longitude)
                            addMarker(posicion, "Pedido de $compradorId")
                            map.controller.setZoom(18.0)
                            map.controller.animateTo(posicion)
                            map.invalidate()
                        } else {
                            Log.w("Geocoder", "No se encontró ubicación para: $direccion")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al leer pedidos", exception)
            }
    }

    fun posicionVendedoresFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Pedidos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val lat = document.getDouble("latvendedor")
                    val lng = document.getDouble("longvendedor")
                    val vendedorID = document.getString("vendedorID") ?: "Desconocido"

                    if (lat != null && lng != null) {
                        val location = LatLng(lat, lng)
                        val direccion = findAddress(location) ?: "Ubicación desconocida"
                        posicionVendedor = GeoPoint(lat, lng)

                        Log.d("Vendedor", "Vendedor: $vendedorID, Dirección: $direccion")
                        if (ultimaPosicionVendedor == null || ultimaPosicionVendedor != posicionVendedor){
                            marcadorVendedor?.let {
                                map.overlays.remove(it)
                            }
                            markerVendedor(posicionVendedor, "Vendedor $vendedorID: $direccion")
                            map.controller.setZoom(18.0)
                            map.controller.animateTo(posicionVendedor)
                            map.invalidate()
                            ultimaPosicionVendedor=posicionVendedor
                        }
                    } else {
                        Log.w("Firestore", "Coordenadas de vendedor faltantes en documento ${document.id}")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al leer vendedores", exception)
            }
    }

    /**
     * Dibuja una ruta en rojo desde un punto inicial hasta un final.
     * @param start Punto inicial
     * @param finish Punto final
     */
    private var roadOverlay: Polyline? = null
    fun drawRoute(start : GeoPoint, finish : GeoPoint){
        var routePoints = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)
        val road = roadManager.getRoad(routePoints)
        Log.i("MapsApp", "Route length: "+road.mLength+" klm")
        Log.i("MapsApp", "Duration: "+road.mDuration/60+" min")
        if(map!=null){
            if(roadOverlay != null){
                map.getOverlays().remove(roadOverlay);
            }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay!!.getOutlinePaint().setColor(Color.RED)
            roadOverlay!!.getOutlinePaint().setStrokeWidth(10F)
            map.getOverlays().add(roadOverlay)
        }
    }

}