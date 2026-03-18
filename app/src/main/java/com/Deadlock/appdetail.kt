@file:Suppress("PackageName")
package com.Deadlock

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(app: AppRiskInfo, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var showAllPermissions by remember { mutableStateOf(false) }

    val riskColor = when (app.riskLevel) {
        "High" -> HighRiskRed
        "Medium" -> MediumRiskYellow
        else -> NoRiskGreen
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        TopAppBar(
            title = { Text("Privacy Audit", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- TOP CARD (Phase 3.1) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                        update = { it.setImageDrawable(it.context.packageManager.getApplicationIcon(app.packageName)) },
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(app.appName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(app.packageName, color = TextSecondary, fontSize = 14.sp)
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${app.riskScore}/10",
                            color = riskColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("RISK SCORE", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(app.riskLevel.uppercase(), color = riskColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- WHY IS THIS RISKY? (Phase 6) ---
            Text("Why is this risky?", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = riskColor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(app.riskExplanation, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- PERMISSIONS SECTIONS (Phase 3.3) ---
            val grouped = groupPermissions(app.grantedPermissions)
            
            PermissionSection("🔴 Critical Access", grouped.critical, true)
            PermissionSection("🟡 Sensitive Access", grouped.sensitive, true)
            
            AnimatedVisibility(visible = showAllPermissions) {
                Column {
                    PermissionSection("🟢 Basic Access", grouped.basic, false)
                    PermissionSection("⚪ Other Permissions", grouped.other, false)
                }
            }

            TextButton(
                onClick = { showAllPermissions = !showAllPermissions },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (showAllPermissions) "Show Less" else "Show All Permissions", color = SamsungBlue)
                Icon(
                    if (showAllPermissions) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SamsungBlue
                )
            }

            if (!app.isRealGranted) {
                Text(
                    "Note: Permissions shown as 'Requested' (Not necessarily granted) for this app version.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SamsungBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("MANAGE PERMISSIONS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PermissionSection(title: String, permissions: List<String>, isImportant: Boolean) {
    if (permissions.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        permissions.forEach { permission ->
            val cleanName = permission.substringAfterLast(".")
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(if(isImportant) HighRiskRed else NoRiskGreen))
                    Spacer(Modifier.width(12.dp))
                    Text(cleanName, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

private data class GroupedPermissions(
    val critical: List<String>,
    val sensitive: List<String>,
    val basic: List<String>,
    val other: List<String>
)

private fun groupPermissions(permissions: List<String>): GroupedPermissions {
    val critical = mutableListOf<String>()
    val sensitive = mutableListOf<String>()
    val basic = mutableListOf<String>()
    val other = mutableListOf<String>()

    permissions.forEach { p ->
        when (p) {
            "android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_SMS", "android.permission.SEND_SMS", "android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG" -> critical.add(p)
            
            "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.ACCESS_COARSE_LOCATION" -> sensitive.add(p)
            
            "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_WIFI_STATE" -> basic.add(p)
            
            else -> other.add(p)
        }
    }
    return GroupedPermissions(critical, sensitive, basic, other)
}
