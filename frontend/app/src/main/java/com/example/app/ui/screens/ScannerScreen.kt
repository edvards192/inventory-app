package com.example.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.ImageAnalysis
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.viewmodel.ScannerViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(onItemFound: (Int) -> Unit, onCreateItem: (String) -> Unit) {

    val context = LocalContext.current
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var scanned by remember { mutableStateOf(false) }
    val viewModel: ScannerViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    if (hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).also {
                            previewView.value = it
                        }
                    }
                )
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val frameWidth = size.width * 0.8f
                val frameHeight = size.height * 0.25f

                val left = (size.width - frameWidth) / 2
                val top = (size.height - frameHeight) / 2

                val right = left + frameWidth
                val bottom = top + frameHeight

                val corner = frameWidth * 0.12f
                val color = Color.White
                val stroke = 8f

                // Top left
                drawLine(
                    color,
                    Offset(left, top),
                    Offset(left + corner, top),
                    stroke
                )
                drawLine(
                    color,
                    Offset(left, top),
                    Offset(left, top + corner),
                    stroke
                )

                // Top right
                drawLine(
                    color,
                    Offset(right, top),
                    Offset(right - corner, top),
                    stroke
                )
                drawLine(
                    color,
                    Offset(right, top),
                    Offset(right, top + corner),
                    stroke
                )

                // Bottom left
                drawLine(
                    color,
                    Offset(left, bottom),
                    Offset(left + corner, bottom),
                    stroke
                )
                drawLine(
                    color,
                    Offset(left, bottom),
                    Offset(left, bottom - corner),
                    stroke
                )

                // Bottom right
                drawLine(
                    color,
                    Offset(right, bottom),
                    Offset(right - corner, bottom),
                    stroke
                )
                drawLine(
                    color,
                    Offset(right, bottom),
                    Offset(right, bottom - corner),
                    stroke
                )
            }
            }
        } else {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera permission required.")
            }
        }
        LaunchedEffect(
            state.navigateToItemId,
            state.navigateToCreateEan,
            state.error
        ) {

            state.navigateToItemId?.let {
                scanned = false
                onItemFound(it)
                viewModel.clearNavigation()
            }
            state.navigateToCreateEan?.let {
                scanned = false
                onCreateItem(it)
                viewModel.clearNavigation()
            }
            state.error?.let {
                println("ERROR: $it")
                scanned = false
                viewModel.clearNavigation()
            }
        }
        LaunchedEffect(
            hasCameraPermission,
            previewView.value
        ) {

            val pv = previewView.value ?: return@LaunchedEffect
            if (!hasCameraPermission)
                return@LaunchedEffect

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val imageAnalysis = ImageAnalysis.Builder().build()
                val scanner = BarcodeScanning.getClient()
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image =
                            InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                        scanner
                            .process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    if (scanned)
                                        break
                                    val ean = barcode.rawValue ?: continue
                                    scanned = true
                                    viewModel.onBarcodeScanned(ean)
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
                preview.surfaceProvider =
                    pv.surfaceProvider

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            }, ContextCompat.getMainExecutor(context))
    }
}