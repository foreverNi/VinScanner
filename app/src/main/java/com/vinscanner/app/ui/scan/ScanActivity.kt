package com.vinscanner.app.ui.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var torchButton: ImageButton
    private var isScanning = false
    private var torchOn = false
    private var torchAvailable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "ScanActivity onCreate")
        try {
            setContentView(R.layout.activity_scan)
        } catch (error: RuntimeException) {
            Log.e(TAG, "Failed to inflate scan layout", error)
            showFatalScanError(
                titleRes = R.string.error_scan_init_title,
                message = getString(R.string.error_scan_init_message, error.message ?: error.javaClass.simpleName)
            )
            return
        }
        barcodeView = findViewById(R.id.barcode_scanner)
        hintView = findViewById(R.id.scan_hint)
        torchButton = findViewById(R.id.torch_button)
        setupTorch()
        barcodeView.getBarcodeView().addStateListener(object : CameraPreview.StateListener {
            override fun previewSized() = Unit
            override fun previewStarted() = Unit
            override fun previewStopped() = Unit
            override fun cameraClosed() = Unit

            override fun cameraError(error: Exception) {
                Log.e(TAG, "Camera error", error)
                runOnUiThread {
                    stopScanning()
                    showFatalScanError(
                        titleRes = R.string.error_camera_title,
                        message = getString(
                            R.string.error_camera_message,
                            error.message ?: error.javaClass.simpleName
                        )
                    )
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
                Log.i(TAG, "Camera permission granted")
                startScanning()
            } else {
                Log.w(TAG, "Camera permission denied")
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

    private fun setupTorch() {
        barcodeView.setTorchListener(object : DecoratedBarcodeView.TorchListener {
            override fun onTorchOn() {
                Log.i(TAG, "Torch on")
            }

            override fun onTorchOff() {
                Log.i(TAG, "Torch off")
            }
        })
        // 部分设备/模拟器无闪光灯：通过 PackageManager 检测，无则禁用按钮
        torchAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!torchAvailable) {
            torchButton.isEnabled = false
            torchButton.alpha = 0.4f
        }
        torchButton.setOnClickListener {
            if (!torchAvailable) {
                Toast.makeText(this, R.string.toast_torch_unavailable, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            toggleTorch()
        }
        updateTorchButton()
    }

    private fun toggleTorch() {
        torchOn = !torchOn
        try {
            if (torchOn) {
                barcodeView.setTorchOn()
            } else {
                barcodeView.setTorchOff()
            }
        } catch (error: RuntimeException) {
            Log.e(TAG, "Torch toggle failed", error)
            torchOn = false
            torchAvailable = false
            torchButton.isEnabled = false
            torchButton.alpha = 0.4f
            Toast.makeText(this, R.string.toast_torch_unavailable, Toast.LENGTH_SHORT).show()
        }
        updateTorchButton()
    }

    private fun updateTorchButton() {
        if (torchOn) {
            torchButton.setImageResource(R.drawable.ic_flash_on)
            torchButton.contentDescription = getString(R.string.action_torch_off)
        } else {
            torchButton.setImageResource(R.drawable.ic_flash_off)
            torchButton.contentDescription = getString(R.string.action_torch_on)
        }
    }

    private fun startScanning() {
        if (isScanning) return
        Log.i(TAG, "Starting scan")
        isScanning = true
        try {
            barcodeView.decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    val text = result?.text ?: return
                    val vin = VinValidator.normalize(text)
                    if (vin.isBlank()) return
                    Log.i(TAG, "Scan result received")
                    stopScanning()
                    val data = Intent().apply { putExtra(EXTRA_SCAN_RESULT, vin) }
                    setResult(RESULT_OK, data)
                    finish()
                }

                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
            })
            barcodeView.resume()
        } catch (error: RuntimeException) {
            Log.e(TAG, "Failed to start scan", error)
            isScanning = false
            showFatalScanError(
                titleRes = R.string.error_scan_init_title,
                message = getString(R.string.error_scan_init_message, error.message ?: error.javaClass.simpleName)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission() && ::barcodeView.isInitialized) {
            // 重新进入时手电筒已随 preview 暂停熄灭，状态复位为默认关闭
            torchOn = false
            if (::torchButton.isInitialized) updateTorchButton()
            startScanning()
        }
    }

    override fun onPause() {
        super.onPause()
        stopScanning()
    }

    private fun stopScanning() {
        if (!::barcodeView.isInitialized) return
        Log.i(TAG, "Stopping scan")
        barcodeView.getBarcodeView().stopDecoding()
        barcodeView.pause()
        isScanning = false
    }

    private fun showFatalScanError(titleRes: Int, message: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    companion object {
        private const val TAG = "VinScannerScan"
        const val REQ_CAMERA = 1001
        const val EXTRA_SCAN_RESULT = "extra_scan_result"
    }
}
