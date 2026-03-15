package com.defender.defcam


import java.nio.ByteOrder
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // 🔴 CHANGE TO YOUR ENGINE IP
    private val TARGET_IP = "100.122.74.118"
    private val PORT = 8888

    private var socket: Socket? = null
    private var output: DataOutputStream? = null
    private var input: DataInputStream? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previewView = PreviewView(this)
        setContentView(FrameLayout(this).apply { addView(previewView) })

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        connectSocket()
    }

    // ================= SOCKET =================

    private fun connectSocket() {
        Thread {
            while (true) {
                try {
                    Log.d("DEF_CAM", "Connecting to engine...")
                    socket = Socket(TARGET_IP, PORT)
                    output = DataOutputStream(socket!!.getOutputStream())
                    input = DataInputStream(socket!!.getInputStream())
                    Log.d("DEF_CAM", "Connected!")
                    break
                } catch (e: Exception) {
                    Log.e("DEF_CAM", "Retrying socket...")
                    Thread.sleep(3000)
                }
            }
        }.start()
    }

    // ================= CAMERA =================

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )

            startStreaming()

        }, ContextCompat.getMainExecutor(this))
    }

    // ================= STREAM LOOP =================

    private fun startStreaming() {
        val handler = android.os.Handler(mainLooper)

        val runnable = object : Runnable {
            override fun run() {
                captureAndSend()
                handler.postDelayed(this, 1000) // every 1 sec
            }
        }

        handler.post(runnable)
    }

    private fun captureAndSend() {
        val file = File(cacheDir, "frame.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    sendFrame(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("DEF_CAM", "Capture error: ${exception.message}")
                }
            })
    }

    private fun sendFrame(file: File) {
        Thread {
            try {
                val bytes: ByteArray = file.readBytes()

                val buffer = ByteBuffer
                    .allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(bytes.size)

                output?.write(buffer.array())
                output?.write(bytes)

                val count = input?.readInt() ?: return@Thread
                Log.d("DEF_CAM", "Detections: $count")

            } catch (e: Exception) {
                Log.e("DEF_CAM", "Stream error: ${e.message}")
                connectSocket()
            }
        }.start()
    }}