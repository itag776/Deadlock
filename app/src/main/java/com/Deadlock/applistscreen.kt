@file:Suppress("PackageName")
package com.Deadlock

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: ScannerViewModel = viewModel(),
    initialTab: Int = 0,
    onBackClick: () -> Unit,
    onAppClick: (AppRiskInfo) -> Unit
) {
    val uiState = viewModel.uiState
    val apps = (uiState as? ScannerUiState.Success)?.apps ?: emptyList()

    var selectedTabIndex by remember(initialTab) { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = apps.filter { app ->
        val matchesTab = when (selectedTabIndex) {
            0 -> app.riskScore >= 10
            1 -> app.riskScore in 5..9
            2 -> app.riskScore in 1..4
            else -> app.riskScore == 0
        }
        val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        TopAppBar(
            title = { Text("PERMISSIONS", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
        )

        val tabs = listOf("High", "Medium", "Low", "Safe")
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = AppBackground,
            contentColor = Color.White,
            indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]), color = Color.White) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index },
                    text = { Text(title, color = if(selectedTabIndex == index) Color.White else Color.Gray) })
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardBackground).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                BasicTextField(value = searchQuery, onValueChange = { searchQuery = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    cursorBrush = SolidColor(Color.White), modifier = Modifier.fillMaxWidth())
            }
        }

        if (uiState is ScannerUiState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SamsungBlue)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredApps) { app ->
                    RiskAppRow(app = app, onClick = { onAppClick(app) })
                }
            }
        }
    }
}

@Composable
fun RiskAppRow(app: AppRiskInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBackground).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(
            factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
            update = { imageView ->
                try {
                    val pm = imageView.context.packageManager
                    imageView.setImageDrawable(pm.getApplicationIcon(app.packageName))
                } catch (e: Exception) { /* Fallback */ }
            },
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))
        Text(text = app.appName, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)

        if (app.riskScore > 0) {
            Icon(Icons.Rounded.Warning, contentDescription = null,
                tint = if(app.riskScore >= 10) HighRiskRed else if(app.riskScore >= 5) MediumRiskYellow else LowRiskBlue)
        }
    }
}
