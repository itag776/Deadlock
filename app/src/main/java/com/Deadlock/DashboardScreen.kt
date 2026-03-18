@file:Suppress("PackageName")
package com.Deadlock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ScannerViewModel = viewModel(),
    onNavigateToList: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    var lastScanned by remember { mutableStateOf("Never") }

    LaunchedEffect(uiState) {
        if (uiState is ScannerUiState.Success) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            lastScanned = sdf.format(Date())
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        CenterAlignedTopAppBar(
            title = { 
                Text(
                    "DEADLOCK AUDIT", 
                    color = Color.White, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                ) 
            },
            actions = {
                IconButton(
                    onClick = { viewModel.startAudit(refresh = true) }, 
                    enabled = uiState !is ScannerUiState.Loading
                ) {
                    if (uiState is ScannerUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppBackground)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is ScannerUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center), 
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SamsungBlue, strokeWidth = 3.dp)
                        Spacer(Modifier.height(20.dp))
                        Text("Analyzing privacy risk...", color = TextSecondary, fontSize = 14.sp)
                    }
                }
                is ScannerUiState.Success -> {
                    DashboardContent(
                        apps = uiState.apps, 
                        lastScanned = lastScanned, 
                        onNavigateToList = onNavigateToList
                    )
                }
                is ScannerUiState.Empty -> {
                    Text("No launchable apps found", modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
                is ScannerUiState.Error -> {
                    Text("Error: ${uiState.message}", modifier = Modifier.align(Alignment.Center), color = DangerRed)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DashboardContent(apps: List<AppRiskInfo>, lastScanned: String, onNavigateToList: (Int) -> Unit) {
    val highCount = apps.count { it.riskLevel == "High" }
    val medCount = apps.count { it.riskLevel == "Medium" }
    val lowCount = apps.count { it.riskLevel == "Low" }
    
    val totalApps = apps.size.toFloat().coerceAtLeast(1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("Security Status", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Detected ${apps.size} user apps", color = TextSecondary, fontSize = 13.sp)
            }
            Text("Updated: $lastScanned", color = TextSecondary, fontSize = 11.sp)
        }
        
        Spacer(Modifier.height(16.dp))

        // PHASE 5: FIX LAYOUT (NO HORIZONTAL SCROLL)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskStatCard(
                modifier = Modifier.weight(1f),
                title = "High Risk", 
                count = highCount.toString(), 
                color = HighRiskRed, 
                progress = highCount / totalApps
            ) { onNavigateToList(0) }
            
            RiskStatCard(
                modifier = Modifier.weight(1f),
                title = "Medium", 
                count = medCount.toString(), 
                color = MediumRiskYellow, 
                progress = medCount / totalApps
            ) { onNavigateToList(1) }
            
            RiskStatCard(
                modifier = Modifier.weight(1f),
                title = "Safe", 
                count = lowCount.toString(), 
                color = LowRiskBlue, 
                progress = lowCount / totalApps
            ) { onNavigateToList(2) }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Quick Filters", 
            color = Color.White, 
            fontSize = 17.sp, 
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ActionCard(
                modifier = Modifier.weight(1f), 
                title = "View Safe Apps", 
                icon = Icons.Rounded.CheckCircle, 
                tint = NoRiskGreen
            ) { onNavigateToList(3) }
        }
    }
}

@Composable
fun RiskStatCard(
    modifier: Modifier = Modifier,
    title: String, 
    count: String, 
    color: Color, 
    progress: Float, 
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(count, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress }, 
                color = color, 
                trackColor = Color(0xFF2D313E), 
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier, title: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tint.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
