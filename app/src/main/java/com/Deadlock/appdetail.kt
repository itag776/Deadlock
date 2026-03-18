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
import androidx.compose.ui.text.style.TextAlign
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
            title = { Text("Privacy Analysis", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PHASE 6: CENTERED TOP CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                            update = { it.setImageDrawable(it.context.packageManager.getApplicationIcon(app.packageName)) },
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = app.appName, 
                        color = Color.White, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = app.packageName, 
                        color = TextSecondary, 
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${app.riskScore}",
                            color = riskColor,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "/10",
                            color = TextSecondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("RISK LEVEL", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(app.riskLevel.uppercase(), color = riskColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- PHASE 6: WHY IS THIS RISKY? IMPROVEMENTS ---
            Text(
                text = "Privacy Assessment", 
                color = Color.White, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp), 
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Rounded.Warning, 
                        contentDescription = null, 
                        tint = riskColor, 
                        modifier = Modifier.size(24.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = app.riskExplanation, 
                        color = TextPrimary, 
                        fontSize = 15.sp, 
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- PHASE 5: HUMAN READABLE PERMISSIONS ---
            val grouped = groupPermissions(app.grantedPermissions)
            
            PermissionSection("CRITICAL ACCESS", grouped.critical, HighRiskRed)
            PermissionSection("SENSITIVE ACCESS", grouped.sensitive, MediumRiskYellow)
            
            AnimatedVisibility(visible = showAllPermissions) {
                Column {
                    PermissionSection("BASIC ACCESS", grouped.basic, NoRiskGreen)
                }
            }

            if (grouped.basic.isNotEmpty()) {
                TextButton(
                    onClick = { showAllPermissions = !showAllPermissions },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = if (showAllPermissions) "Hide Technical Details" else "Show All Details", 
                        color = SamsungBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        if (showAllPermissions) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SamsungBlue
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Footer note for transparency
            if (!app.isRealGranted) {
                Text(
                    text = "Note: Permissions shown as requested (standard for this app version).",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SamsungBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("MANAGE PERMISSIONS", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun PermissionSection(title: String, permissions: List<PermissionDetail>, accentColor: Color) {
    if (permissions.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Text(
            text = title, 
            color = TextSecondary, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        permissions.forEach { detail ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(accentColor, RoundedCornerShape(5.dp))
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = detail.readableName, 
                        color = Color.White, 
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private data class GroupedPermissions(
    val critical: List<PermissionDetail>,
    val sensitive: List<PermissionDetail>,
    val basic: List<PermissionDetail>
)

private fun groupPermissions(permissions: List<PermissionDetail>): GroupedPermissions {
    return GroupedPermissions(
        critical = permissions.filter { it.severity == "Critical" },
        sensitive = permissions.filter { it.severity == "Sensitive" },
        basic = permissions.filter { it.severity == "Basic" }
    )
}
