package com.example.alpai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.concurrent.Executors

// Enum untuk mengatur Navigasi Sederhana
enum class Screen {
    MENU, LIVE_PREDICT, STATIC_IMAGE
}

class MainActivity : ComponentActivity() {
    private lateinit var classifier: ClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifier = ClassifierHelper(this)

        setContent {
            // Panggil App Utama yang berisi Logika Navigasi
            MainApp(classifier)
        }
    }

    override fun onDestroy() {
        classifier.close()
        super.onDestroy()
    }
}

@Composable
fun MainApp(classifier: ClassifierHelper) {
    // State untuk menentukan layar mana yang aktif
    var currentScreen by remember { mutableStateOf(Screen.MENU) }

    // Logika perpindahan layar
    when (currentScreen) {
        Screen.MENU -> {
            MenuScreen(
                onNavigateToLive = { currentScreen = Screen.LIVE_PREDICT },
                onNavigateToStatic = { currentScreen = Screen.STATIC_IMAGE }
            )
        }
        Screen.LIVE_PREDICT -> {
            CameraLiveScreen(
                classifier = classifier,
                onBack = { currentScreen = Screen.MENU }
            )
        }
        Screen.STATIC_IMAGE -> {
            StaticImageScreen(
                classifier = classifier,
                onBack = { currentScreen = Screen.MENU }
            )
        }
    }
}

// --- 1. LAYAR MENU UTAMA ---
@Composable
fun MenuScreen(onNavigateToLive: () -> Unit, onNavigateToStatic: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Klasifikasi Sampah",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Tombol ke Live Prediction
        Button(
            onClick = onNavigateToLive,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("📹 Live Camera Detection", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol ke Foto/Galeri (Satu layar yang sama)
        Button(
            onClick = onNavigateToStatic,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("📷 Ambil Foto / Galeri", fontSize = 18.sp)
        }
    }
}

// --- 2. LAYAR LIVE PREDICTION (Realtime) ---
@Composable
fun CameraLiveScreen(classifier: ClassifierHelper, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentPrediction by remember { mutableStateOf("Mendeteksi...") }

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

    if (hasPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            // CameraX Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    val bitmap = imageProxy.toBitmap()
                                    if (bitmap != null) {
                                        currentPrediction = classifier.classify(bitmap)
                                    }
                                    imageProxy.close()
                                }
                            }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                        } catch (e: Exception) { Log.e("CameraX", "Error", e) }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay Hasil
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp)
            ) {
                Text(text = currentPrediction, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }

            // Tombol Kembali (Pojok Kiri Atas)
            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Text("Kembali")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Perlu Izin Kamera") }
    }
}

// --- 3. LAYAR STATIC IMAGE (Foto & Galeri) ---
@Composable
fun StaticImageScreen(classifier: ClassifierHelper, onBack: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("Silakan ambil foto atau pilih gambar") }

    // Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bmp = uriToBitmap(context, it)
            bitmap = bmp
            bmp?.let { b -> resultText = classifier.classify(b) }
        }
    }

    // Launcher Kamera (Foto)
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            bitmap = bmp
            resultText = classifier.classify(bmp)
        }
    }

    // Izin Kamera
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tombol Kembali
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Text("< Menu")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tampilan Gambar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Hasil Foto",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Gambar akan muncul di sini", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Teks Hasil
        Text(
            text = resultText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Aksi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Buka Galeri")
            }

            Button(onClick = {
                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text("Ambil Foto")
            }
        }
    }
}

// Helper: Uri ke Bitmap
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}