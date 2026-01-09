package com.example.alpai.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alpai.enmuclass.WasteCategory
import com.example.alpai.viewmodel.ClassifierHelper
import com.example.alpai.model.ImageUtils
import com.example.alpai.model.WasteResult
import com.example.alpai.ui.theme.GreenPrimary
import com.example.alpai.ui.theme.GreenSecondary
import com.example.alpai.ui.theme.GreenBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticImageScreen(classifier: ClassifierHelper, onBack: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var result by remember { mutableStateOf<WasteResult?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bmp = ImageUtils.uriToBitmap(context, it)
            bitmap = bmp
            bmp?.let { b -> result = classifier.classify(b) }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            bitmap = bmp
            result = classifier.classify(bmp)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analisis Gambar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = GreenPrimary,
                    navigationIconContentColor = GreenPrimary
                )
            )
        },
        containerColor = GreenBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Bingkai Gambar Preview
                Card(
                    modifier = Modifier
                        .size(280.dp), // Ukuran kotak preview yang seimbang
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(28.dp)),
                                contentScale = ContentScale.Crop // Crop agar rapi di dalam kotak
                            )
                        } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼️", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Belum ada gambar",
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Panel Hasil Analisis
                if (result != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // TITLE
                        Text(
                            text = "HASIL KLASIFIKASI",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )

                        // CATEGORY NAME
                        Text(
                            text = result!!.category.displayName,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = GreenPrimary,
                            textAlign = TextAlign.Center
                        )

                        // 🔴 KHUSUS HUMAN
                        if (result!!.category == WasteCategory.HUMAN) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Objek ini bukan sampah",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // LABEL + CONFIDENCE
                        Surface(
                            color = GreenSecondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${result!!.label} (${String.format("%.1f%%", result!!.confidence * 100)})",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }

                        // IMAGE (KECUALI UNKNOWN)
                        if (result!!.category != WasteCategory.UNKNOWN) {
                            result!!.category.binImage?.let { img ->
                                Spacer(modifier = Modifier.height(20.dp))
                                Image(
                                    painter = painterResource(img),
                                    contentDescription = null,
                                    modifier = Modifier.height(120.dp)
                                )
                            }
                        }
                    }
                } else {
                    // EMPTY STATE (TETAP ADA)
                    Text(
                        text = "Ambil foto sampah Anda untuk mengetahui kategori pembuangannya",
                        textAlign = TextAlign.Center,
                        color = Color.Gray.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }


                Spacer(modifier = Modifier.height(48.dp))

                // Tombol Aksi di Bagian Bawah
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary)
                    ) {
                        Text("📁 Galeri", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("📸 Kamera", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
