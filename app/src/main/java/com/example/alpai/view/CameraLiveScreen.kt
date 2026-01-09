package com.example.alpai.view

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.alpai.enmuclass.WasteCategory
import com.example.alpai.model.WasteResult
import com.example.alpai.viewmodel.ClassifierHelper
import java.util.concurrent.Executors

@Composable
fun CameraLiveScreen(classifier: ClassifierHelper, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var result by remember { mutableStateOf<WasteResult?>(null) }

    // Cek Izin Kamera
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Izin kamera diperlukan")
        }
        return
    }

    Box(Modifier.fillMaxSize()) {

        AndroidView(
            factory = {
                val previewView = PreviewView(it).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(it)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                                val bitmap = image.toBitmap()
                                bitmap?.let { b ->
                                    result = classifier.classify(b)
                                }
                                image.close()
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analyzer
                    )
                }, ContextCompat.getMainExecutor(it))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        result?.let {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Objek: ${it.label}", color = Color.White)

                Text(
                    it.category.displayName,
                    color = Color.Yellow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // ❌ HUMAN = BUKAN SAMPAH
                if (it.category == WasteCategory.HUMAN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Objek ini bukan sampah",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )

                    it.category.binImage?.let { img ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            painter = painterResource(img),
                            contentDescription = null,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                } else if (it.category != WasteCategory.UNKNOWN) {
                    it.category.binImage?.let { img ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            painter = painterResource(img),
                            contentDescription = null,
                            modifier = Modifier.height(100.dp)
                        )
                    }

                    Text(
                        "Confidence: ${String.format("%.1f%%", it.confidence * 100)}",
                        color = Color.LightGray
                    )
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Kembali")
        }
    }
}