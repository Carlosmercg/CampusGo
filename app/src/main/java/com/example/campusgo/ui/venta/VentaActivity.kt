package com.example.campusgo.ui.venta

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.campusgo.R
import com.example.campusgo.data.models.Producto
import com.example.campusgo.data.models.Usuario
import com.example.campusgo.databinding.ActivityVentaBinding
import com.example.campusgo.ui.adapters.ProductoAdapter
import com.example.campusgo.ui.home.HomeActivity
import com.example.campusgo.ui.mapas.MapaVendedorActivity

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class VentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentaBinding
    private lateinit var productoAdapter: ProductoAdapter
    private val productosVenta = mutableListOf<Producto>()
    lateinit var map: MapView
    private lateinit var posicion: GeoPoint
    private lateinit var direccion: String
    val bogota = GeoPoint(4.62, -74.07)


    private lateinit var geocoder: Geocoder
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightEventListener: SensorEventListener

    override fun onResume() {
        super.onResume()
        map.onResume()
        map.controller.setZoom(18.0)
        map.controller.animateTo(bogota)

        val uims = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uims.nightMode == UiModeManager.MODE_NIGHT_YES) {

            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }

        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        sensorManager.unregisterListener(lightEventListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pedidoId = intent.getStringExtra("pedidoID") ?: "-"
        val db = FirebaseFirestore.getInstance()

        map = binding.osmap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        binding.osmap.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    binding.osmap.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    binding.osmap.parent?.requestDisallowInterceptTouchEvent(false)
                    view.performClick()
                }
            }
            false
        }

        // Botón Aceptar
        binding.btnAceptar.setOnClickListener {
            db.collection("Pedidos").document(pedidoId)
                .update("estado", "aceptado")
                .addOnSuccessListener {
                    Toast.makeText(this, "Pedido aceptado", Toast.LENGTH_SHORT).show()

                    val uidVendedor = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    db.collection("Pedidos").document(pedidoId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val uidComprador = doc.getString("compradorId") ?: ""
                            val notisCompraRef = FirebaseDatabase.getInstance()
                                .getReference("notificacionesCompra")
                                .child(uidComprador)
                            val noti = hashMapOf<String, Any>(
                                "uidVendedor" to uidVendedor,
                                "productoId"  to doc.getString("productoId").orEmpty(),
                                "chatId"      to pedidoId,
                                "resultado"   to "aceptada",
                                "titulo"      to "¡Tu compra fue aceptada!",
                                "body"        to "Toca para ver tu producto en el mapa"
                            )
                            notisCompraRef.push().setValue(noti)
                        }
                        .addOnFailureListener {
                        }
                    val intent = Intent(this, MapaVendedorActivity::class.java)
                    intent.putExtra("pedidoID", pedidoId)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al aceptar pedido", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnRechazar.setOnClickListener {
            db.collection("Pedidos").document(pedidoId)
                .update("estado", "rechazado")
                .addOnSuccessListener {
                    Toast.makeText(this, "Pedido rechazado", Toast.LENGTH_SHORT).show()
                    val uidVendedor = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    db.collection("Pedidos").document(pedidoId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val uidComprador = doc.getString("compradorId") ?: ""
                            val notisCompraRef = FirebaseDatabase.getInstance()
                                .getReference("notificacionesCompra")
                                .child(uidComprador)
                            val noti = hashMapOf<String, Any>(
                                "uidVendedor" to uidVendedor,
                                "productoId"  to doc.getString("productoId").orEmpty(),
                                "chatId"      to pedidoId,
                                "resultado"   to "rechazada",
                                "titulo"      to "Lo sentimos, tu compra fue rechazada",
                                "body"        to "Toca para iniciar chat con el vendedor"
                            )
                            notisCompraRef.push().setValue(noti)
                        }
                        .addOnFailureListener {
                        }
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al rechazar pedido", Toast.LENGTH_SHORT).show()
                }
        }

        geocoder = Geocoder(baseContext)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        lightEventListener = createLightSensorListener()

        map = binding.osmap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        binding.osmap.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    binding.osmap.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    binding.osmap.parent?.requestDisallowInterceptTouchEvent(false)
                    view.performClick()
                }
            }
            false
        }



        geocoder = Geocoder(baseContext)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        lightEventListener = createLightSensorListener()

        // Flecha atrás
        binding.ivBack.setOnClickListener { onBackPressed() }

        // RecyclerView
        productoAdapter = ProductoAdapter(productosVenta) { /* click opcional */ }
        binding.rvProductosVenta.apply {
            layoutManager = LinearLayoutManager(this@VentaActivity)
            adapter = productoAdapter
        }

        // Carga de datos desde Firestore
        FirebaseFirestore.getInstance()
            .collection("Pedidos")
            .document(pedidoId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 1) Dirección
                binding.tvDireccion.text = doc.getString("direccion") ?: "-"
                direccion= doc.getString("direccion") ?: "-"
                val location = findLocation(direccion)
                if (location != null) {
                    posicion = location
                    addMarker(posicion, direccion)
                    map.controller.animateTo(posicion)
                } else {
                    Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                }

                // 2) Hora
                doc.getTimestamp("fecha")?.toDate()?.let { date ->
                    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    binding.tvHora.text = fmt.format(date)
                }

                // 3) Método de pago
                val metodo = doc.getString("metodoPago") ?: ""
                binding.tvMetodoText.text = metodo.uppercase(Locale.getDefault())
                val iconRes = if (metodo.equals("tarjeta", true))
                    R.drawable.credit_card
                else
                    R.drawable.cash
                binding.imgIconPago.setImageResource(iconRes)

                // 4) Productos embebidos
                @Suppress("UNCHECKED_CAST")
                val listaMap = doc.get("productos") as? List<Map<String, Any>> ?: emptyList()
                val listaProductos = listaMap.map { m ->
                    val rawPrecio = m["precio"]
                    val precio = when (rawPrecio) {
                        is Number -> rawPrecio.toDouble()
                        is String -> rawPrecio.toDoubleOrNull() ?: 0.0
                        else       -> 0.0
                    }
                    Producto(
                        id             = m["id"]                as? String ?: "",
                        nombre         = m["nombre"]            as? String ?: "",
                        vendedorId     = m["vendedorId"]        as? String ?: "",
                        categoriaId    = m["categoriaId"]       as? String ?: "",
                        precio         = precio,
                        imagenUrl      = m["imagenUrl"]         as? String ?: "",
                        vendedorNombre = m["vendedorNombre"]    as? String ?: "",
                        descripcion    = m["descripcion"]       as? String ?: ""
                    )
                }

                // Actualizar lista y adapter
                productosVenta.clear()
                productosVenta.addAll(listaProductos)
                productoAdapter.notifyDataSetChanged()

                // ─── Cálculo de tarifas ────────────────────────────────────────
                val subtotal = listaProductos.sumOf { it.precio }
                val servicio = subtotal * 0.02
                val total    = subtotal + servicio

                // Formateador con dos decimales y separador de miles
                val df = DecimalFormat("#,##0.00")

                binding.tvServicios.text = "$${df.format(servicio)}"
                binding.tvTotal.text     = "Total: $${df.format(total)}"

                // 5) Cargar datos del comprador
                val compradorId = doc.getString("compradorId") ?: ""
                if (compradorId.isNotBlank()) cargarComprador(compradorId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando pedido: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun cargarComprador(uid: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val usr = doc.toObject(Usuario::class.java)
                if (usr != null) {
                    binding.tvCompradorNombre.text = "${usr.nombre} ${usr.apellido}"
                    if (usr.urlFotoPerfil.isNotBlank()) {
                        Glide.with(this)
                            .load(usr.urlFotoPerfil)
                            .placeholder(R.drawable.placeholder_usuario)
                            .error(R.drawable.placeholder_usuario)
                            .into(binding.imgComprador)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando comprador", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Agrega un marcador al mapa.
     * @param p GeoPoint de la posición
     * @param snippet Descripción corta
     * @param longPressed Indica si es de un long press
     */
    private fun addMarker(p: GeoPoint, snippet: String) {
        var addressText = direccion

        val marker = createMarker(
            p, addressText, snippet,
            R.drawable.baseline_add_location_alt_24
        )
        if (marker != null) {
            map.overlays.add(marker)
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
    private fun createMarker(p:GeoPoint, title: String, desc: String, iconID : Int) : Marker? {
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

    /**
     * Busca una ubicación LatLng a partir de una dirección en texto.
     * @param address Dirección como texto
     * @return GeoPoint correspondiente
     */
    fun findLocation(address: String): GeoPoint? {
        val addresses = geocoder.getFromLocationName(address, 2)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            return GeoPoint(addr.latitude, addr.longitude)
        }
        return null
    }
}
