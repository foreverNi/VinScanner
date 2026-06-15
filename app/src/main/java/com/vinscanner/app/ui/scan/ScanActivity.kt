package com.vinscanner.app.ui.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.vinscanner.app.R
import com.vinscanner.app.util.VinValidator

class ScanActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var hintView: TextView
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        barcodeView = findViewById(R.id.barcode_scanner)
        hintView = findViewById(R.id.scan_hint)
        barcodeView.getBarcodeView().addStateListener(object : CameraPreview.StateListener {
            override fun previewSized() = Unit
            override fun previewStarted() = Unit
            override fun previewStopped() = Unit
            override fun cameraClosed() = Unit

            override fun cameraError(error: Exception) {
                runOnUiThread {
                    stopScanning()
                    Toast.makeText(
                        this@ScanActivity,
                        R.string.toast_camera_open_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        })

        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            startScanning()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                startScanning()
            } else {
                Toast.makeText(this, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
    }

    private fun startScanning() {
        if (isScanning) return
        isScanning = true
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                val text = result?.text ?: return
                val vin = VinValidator.normalize(text)
                if (vin.isBlank()) return
                stopScanning()
                val data = Intent().apply { putExtra(EXTRA_SCAN_RESULT, vin) }
                setResult(RESULT_OK, data)
                finish()
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        })
        barcodeView.resume()
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission() && ::barcodeView.isInitialized) {
            startScanning()
        }
    }

    override fun onPause() {
        super.onPause()
        stopScanning()
    }

    private fun stopScanning() {
        if (!::barcodeView.isInitialized) return
        barcodeView.getBarcodeView().stopDecoding()
        barcodeView.pause()
        isScanning = false
    }

    companion object {
        const val REQ_CAMERA = 1001
        const val EXTRA_SCAN_RESULT = "extra_scan_result"
    }
}
