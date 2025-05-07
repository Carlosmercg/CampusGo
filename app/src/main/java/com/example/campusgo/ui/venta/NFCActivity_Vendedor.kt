package com.example.campusgo.ui.venta

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.campusgo.databinding.ActivityNfcvendedorBinding
import com.example.campusgo.ui.compra.Codigo_NFC
import com.example.campusgo.ui.compra.CalificarActivityVendedor

class NFCActivity_Vendedor : AppCompatActivity() {

    private lateinit var binding: ActivityNfcvendedorBinding
    private var nfcAdapter: NfcAdapter? = null


    private val permisoNfcLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) iniciarDispatchNFC() else fallbackConCodigo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcvendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) ¿Tiene NFC?
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            fallbackConCodigo()
            return
        }

        // 2) ¿Está activado?
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Activa el NFC en ajustes", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        // 3) Pedir permiso, o iniciar dispatch
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
        if (nfcAdapter != null &&
            checkSelfPermission(android.Manifest.permission.NFC) == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarDispatchNFC()
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }


    private fun iniciarDispatchNFC() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        nfcAdapter?.enableForegroundDispatch(this, pending, null, null)
        binding.tvEsperando.text = "Acerca tu teléfono…"
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startActivity(Intent(this, CalificarActivityVendedor::class.java))
        finish()
    }


    private fun fallbackConCodigo() {
        startActivity(Intent(this, Codigo_NFC::class.java))
        finish()
    }
}
