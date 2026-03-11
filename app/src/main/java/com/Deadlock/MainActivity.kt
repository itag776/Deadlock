package com.Deadlock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Samsung One UI Dark Mode Colors
val DarkBackground = Color(0xFF000000)
val SurfaceCard = Color(0xFF151515) // Deep gray for rounded cards
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFFA0A0A0)
val SamsungBlue = Color(0xFF3B82F6)
val DangerRed = Color(0xFFEF4444)

// This represents the 4 possible states our screen can be in
sealed interface ScannerUiState {
    object Idle : ScannerUiState
    object Scanning : ScannerUiState
    data class Success(val apps: List<AppRiskInfo>) : ScannerUiState
    object Empty : ScannerUiState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Forcing Dark Mode for the sleek security look
            MaterialTheme(colorScheme = darkColorScheme(background = DarkBackground)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    OneUiDashboard()
                }
            }
        }
    }
}

@Composable
fun OneUiDashboard() {
    val context = LocalContext.current
    var scannedApps by remember { mutableStateOf<List<AppRiskInfo>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Large Samsung-style Header
        Text(
            text = "DeadLock",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 24.dp, start = 8.dp)
        )

        // The "Scan" Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .clickable {
                    val scanner = PermissionScanner(context)
                    scannedApps = scanner.scanApps()
                }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SamsungBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "Scan", tint = SamsungBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Audit Permissions", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Scan device for privacy leaks", color = TextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (scannedApps.isNotEmpty()) {
            Text(
                text = "Audited Applications",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            // The main list inside a large rounded container
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
            ) {
                items(scannedApps) { app ->
                    SettingsAppRow(app = app)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ready to scan.", color = TextSecondary)
            }
        }
    }
}

@Composable
fun SettingsAppRow(app: AppRiskInfo) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Deep link to settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", app.packageName, null)
                }
                context.startActivity(intent)
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App "Icon" Placeholder (First letter of app name)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (app.riskScore == 3) DangerRed.copy(alpha = 0.2f) else Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.appName.take(1).uppercase(),
                color = if (app.riskScore == 3) DangerRed else TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.appName, color = TextPrimary, fontSize = 16.sp)

            // Build the string of what they are using
            val sensors = mutableListOf<String>()
            if (app.hasCamera) sensors.add("Camera")
            if (app.hasMic) sensors.add("Mic")
            if (app.hasLocation) sensors.add("Location")

            Text(text = sensors.joinToString(" • "), color = TextSecondary, fontSize = 13.sp)
        }

        if (app.riskScore == 3) {
            Icon(Icons.Rounded.Warning, contentDescription = "High Risk", tint = DangerRed, modifier = Modifier.size(20.dp))
        }
    }
}
class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    // Hold the UI state here, not in the View
    var uiState by mutableStateOf<ScannerUiState>(ScannerUiState.Idle)
        private set

    private val scanner = PermissionScanner(application)

    fun startAudit() {
        // Prevent double-tapping
        if (uiState is ScannerUiState.Scanning) return

        uiState = ScannerUiState.Scanning

        viewModelScope.launch(Dispatchers.IO) {
            val results = scanner.scanApps()

            withContext(Dispatchers.Main) {
                uiState = if (results.isNotEmpty()) {
                    ScannerUiState.Success(results)
                } else {
                    ScannerUiState.Empty
                }
            }
        }
    }
}