package com.example.campusgo.ui.mapas

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
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusgo.R
import com.example.campusgo.databinding.ActivityMapaCompradorBinding
import com.example.campusgo.databinding.ActivityMapaVendedorBinding
import com.example.campusgo.ui.venta.Codigo_NFC_Comprador

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
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

class MapaVendedorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaVendedorBinding
    lateinit var map: MapView
    private lateinit var posicion: GeoPoint
    private lateinit var posicionEntrega: GeoPoint
    private var currentLocationMarker: Marker? = null
    private var direccionMarker: Marker? = null
    private var lastSavedLocation: GeoPoint? = null
    lateinit var pedidoId : String

    private lateinit var geocoder: Geocoder
    private lateinit var roadManager: RoadManager

    // Radio de la Tierra usado para cálculos de distancia
    val RADIUS_OF_EARTH_KM = 6378

    // Sensor de luz para cambiar el estilo del mapa
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightEventListener: SensorEventListener

    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private lateinit var orientationListener: SensorEventListener

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // Cliente de ubicación y configuraciones
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback

    /**
     * Callback para cuando se activa la ubicación del dispositivo (GPS).
     */
    val locationSettings = registerForActivityResult(
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
    val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ActivityResultCallback {
            if(it){
                locationSettings()
            }
        }
    )

    override fun onResume() {
        super.onResume()
        pedidoId = intent.getStringExtra("pedidoID").toString()
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        map.onResume()
        val uims = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uims.nightMode == UiModeManager.MODE_NIGHT_YES){
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
        // Registra el sensor de luz
        sensorManager.registerListener(lightEventListener, lightSensor,
            SensorManager.SENSOR_DELAY_FASTEST)

        sensorManager.registerListener(orientationListener, accelerometer,
            SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(orientationListener, magnetometer,
            SensorManager.SENSOR_DELAY_UI)

    }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        map.onPause()
        // Detiene el sensor de luz
        sensorManager.unregisterListener(lightEventListener)
        sensorManager.unregisterListener(orientationListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapaVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa servicios de ubicación
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallback()

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

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
        orientationListener = createOrientationListener()

        binding.btnNFC.setOnClickListener{
            startActivity(Intent(this, Codigo_NFC_Comprador::class.java))
        }

    }

    /**
     * Configura la verificación de servicios de ubicación (GPS).
     */
    fun locationSettings(){
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
    /**
     * Crea el objeto de solicitud de actualizaciones de ubicación.
     * @return LocationRequest configurado
     */
    private fun createLocationRequest(): com.google.android.gms.location.LocationRequest {
        val request = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()
        return request
    }
    /**
     * Crea el objeto callback que recibe actualizaciones de ubicación.
     * Guarda ubicaciones periódicamente en un archivo JSON.
     * @return LocationCallback configurado
     */
    private fun createLocationCallback(): LocationCallback {
        val callback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val loc = result.lastLocation
                if(loc != null){
                    val newPosition = GeoPoint(loc.latitude, loc.longitude)
                    posicion = newPosition
                    posicion = GeoPoint(loc.latitude, loc.longitude)
                    addLocationMarker()
                    map.controller.setZoom(18.0)
                    map.controller.animateTo(posicion)
                    updateUI(loc)

                    if (lastSavedLocation == null ||
                        distance(lastSavedLocation!!.latitude, lastSavedLocation!!.longitude,
                            newPosition.latitude, newPosition.longitude) > 0.03
                    ) {
                        if (direccionMarker==null){
                            direccionFirestore()
                        }
                        lifecycleScope.launch {
                            delay(3000)
                            drawRoute(posicion,posicionEntrega)
                        }
                        val firestore = FirebaseFirestore.getInstance()

                        val nuevaUbicacion = mapOf(
                            "latvendedor" to newPosition.latitude,
                            "longvendedor" to newPosition.longitude
                        )

                        firestore.collection("Pedidos").document(pedidoId)
                            .update(nuevaUbicacion)
                        lastSavedLocation = newPosition
                    }
                }
            }
        }
        return callback
    }
    /**
     * Actualiza la posición actual en la interfaz de usuario.
     * @param location Nueva ubicación
     */
    fun updateUI(location : Location){
        Log.i("GPS_APP", "(lat: ${location.latitude}, long: ${location.longitude})")
        posicion = GeoPoint(location.latitude, location.longitude)
    }
    /**
     * Inicia la recepción de actualizaciones de ubicación.
     */
    fun startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    /**
     * Detiene la recepción de actualizaciones de ubicación.
     */
    fun stopLocationUpdates(){
        locationClient.removeLocationUpdates(locationCallback)
    }
    fun addLocationMarker() {
        if (currentLocationMarker != null) {
            map.overlays.remove(currentLocationMarker)
        }

        var direccion = "Dirección desconocida"

        val addresses = geocoder.getFromLocation(posicion.latitude, posicion.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            direccion = addresses[0].getAddressLine(0) ?: "Dirección desconocida"
        }

        val marker = Marker(map)
        marker.title = direccion
        marker.subDescription = "Latitud: ${posicion.latitude}\nLongitud: ${posicion.longitude}"

        val myIcon = resources.getDrawable(R.drawable.baseline_arrow_circle_up_24, theme)
        marker.icon = myIcon
        marker.position = posicion
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map.overlays.add(marker)
        currentLocationMarker = marker
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

    fun createOrientationListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                when (event?.sensor?.type) {
                    Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                    Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
                }

                if (gravity != null && geomagnetic != null) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)

                    if (success) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)

                        val azimut = Math.toDegrees(orientation[0].toDouble()).toFloat()

                        // Rota el marcador si está colocado
                        currentLocationMarker?.rotation = -azimut
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
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
            direccionMarker=marker
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
        val overlayEvents = MapEventsOverlay(object : MapEventsReceiver {
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
            .whereEqualTo("id", pedidoId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val direccion = document.getString("direccion")
                    binding.destino.text=direccion
                    val compradorId = document.getString("compradorID")
                    if (compradorId != null) {
                        buscarNombreComprador(compradorId) { nombre ->
                            binding.Comprador.text = nombre
                        }
                    }
                    if (direccion != null) {
                        val latLng = findLocation(direccion)
                        if (latLng != null) {
                            posicionEntrega = GeoPoint(latLng.latitude, latLng.longitude)
                            addMarker(posicionEntrega, "Pedido de $compradorId")
                            map.controller.setZoom(18.0)
                            map.controller.animateTo(posicionEntrega)
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

    fun buscarNombreComprador(compradorId: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(compradorId)
            .get()
            .addOnSuccessListener { usuarioDoc ->
                val nombre = usuarioDoc.getString("nombre") ?: "Nombre desconocido"
                callback(nombre)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener nombre del comprador", e)
                callback("Nombre desconocido")
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