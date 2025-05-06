package com.example.campusgo.compra

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityNfcactivityBinding

class NFCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNfcactivityBinding
    private var nfcAdapter: NfcAdapter? = null

    // Para pedir permiso NFC en Android 13+
    private val permisoNfcLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) iniciarDispatchNFC() else fallbackConCodigo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) ¿Tiene NFC el dispositivo?
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            // No hay NFC → usamos código manual
            fallbackConCodigo()
            return
        }

        // 2) ¿Está habilitado el NFC?
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Activa el NFC en ajustes", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            // El usuario volverá aquí cuando active NFC
        }

        // 3) Pedir permiso NFC si corresponde (Android 13+)
        if (checkSelfPermission(android.Manifest.permission.NFC)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permisoNfcLauncher.launch(android.Manifest.permission.NFC)
        } else {
            iniciarDispatchNFC()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-habilitar el foreground dispatch si ya tenemos permiso
        if (nfcAdapter != null &&
            checkSelfPermission(android.Manifest.permission.NFC) == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarDispatchNFC()
        }
    }

    override fun onPause() {
        super.onPause()
        // Deshabilitar el foreground dispatch para no interferir con otras apps
        nfcAdapter?.disableForegroundDispatch(this)
    }

    /** Configura el “foreground dispatch” para capturar tags NFC aquí */
    private fun iniciarDispatchNFC() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        nfcAdapter?.enableForegroundDispatch(this, pending, null, null)

        // Actualizamos el texto de espera
        binding.tvEsperando.text = "Acerca tu teléfono…"
    }

    /** Se llama cuando llega un tag NFC */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // NFC detectado → confirmamos y pasamos a calificar
        startActivity(Intent(this, CalificarActivityVendedor::class.java))
        finish()
    }

    /** Si no hay NFC o niegan permiso, vamos a la pantalla de código */
    private fun fallbackConCodigo() {
        startActivity(
            Intent(this, Codigo_NFC::class.java)
                .putExtra("modo", "generar")
        )
        finish()
    }
}
