@file:Suppress("PackageName")
package com.Deadlock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
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
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            lastScanned = sdf.format(Date())
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        CenterAlignedTopAppBar(
            title = { Text("DEADLOCK AUDIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            actions = {
                IconButton(onClick = { viewModel.startAudit(refresh = true) }, enabled = uiState !is ScannerUiState.Loading) {
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
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SamsungBlue)
                        Spacer(Modifier.height(16.dp))
                        Text("Analyzing permissions...", color = TextSecondary)
                    }
                }
                is ScannerUiState.Success -> {
                    DashboardContent(apps = uiState.apps, lastScanned = lastScanned, onNavigateToList = onNavigateToList)
                }
                is ScannerUiState.Empty -> {
                    Text("No apps found", modifier = Modifier.align(Alignment.Center), color = Color.White)
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

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Security Overview", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Total Apps: ${apps.size}", color = TextSecondary, fontSize = 14.sp)
            }
            Text("Last scan: $lastScanned", color = TextSecondary, fontSize = 12.sp)
        }
        
        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item { RiskStatCard("High Risk", highCount.toString(), HighRiskRed, highCount / totalApps) { onNavigateToList(0) } }
            item { RiskStatCard("Medium", medCount.toString(), MediumRiskYellow, medCount / totalApps) { onNavigateToList(1) } }
            item { RiskStatCard("Low Risk", lowCount.toString(), LowRiskBlue, lowCount / totalApps) { onNavigateToList(2) } }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(Modifier.weight(1f), "System", Icons.Rounded.Settings, LowRiskBlue) { onNavigateToList(2) }
            ActionCard(Modifier.weight(1f), "Recent", Icons.Rounded.DateRange, HighRiskRed) { onNavigateToList(0) }
            ActionCard(Modifier.weight(1f), "Safe", Icons.Rounded.CheckCircle, NoRiskGreen) { onNavigateToList(3) }
        }
    }
}

@Composable
fun RiskStatCard(title: String, count: String, color: Color, progress: Float, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(110.dp).clip(RoundedCornerShape(16.dp)).background(CardBackground)
            .clickable { onClick() }.padding(16.dp)
    ) {
        Text(count, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(title, color = Color.White, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress }, 
            color = color, 
            trackColor = Color.DarkGray, 
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionCard(modifier: Modifier, title: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(CardBackground)
            .clickable { onClick() }.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
