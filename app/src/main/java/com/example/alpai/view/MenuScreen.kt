package com.example.alpai.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(onNavigateToLive: () -> Unit, onNavigateToStatic: () -> Unit) {
    // Menggunakan Column sebagai container utama dengan latar belakang lembut
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBF9)) // Warna latar belakang yang bersih
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ikon Visual sebagai Header
        Text(
            text = "♻️",
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Judul Aplikasi
        Text(
            text = "Waste Classifier",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF2D6A4F), // Hijau gelap yang profesional
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Bantu jaga bumi dengan memilah sampah secara tepat",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        // Tombol Deteksi Real-time (Live)
        MenuButton(
            title = "Live Camera Detection",
            subtitle = "Deteksi sampah secara langsung",
            icon = "📹",
            containerColor = Color(0xFF2D6A4F),
            onClick = onNavigateToLive
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tombol Analisis Foto/Galeri (Static)
        MenuButton(
            title = "Ambil Foto / Galeri",
            subtitle = "Analisis gambar dari penyimpanan",
            icon = "📷",
            containerColor = Color(0xFF52B788), // Hijau lebih terang
            onClick = onNavigateToStatic
        )
    }
}

/**
 * tombol kustom bentuk Card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuButton(
    title: String,
    subtitle: String,
    icon: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lingkaran kecil untuk menampung emoji/ikon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
