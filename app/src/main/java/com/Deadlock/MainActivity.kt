@file:Suppress("PackageName")
package com.Deadlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// --- GLOBAL DESIGN SYSTEM COLORS ---
val DarkBackground = Color(0xFF000000)
val AppBackground = Color(0xFF16181F)
val CardBackground = Color(0xFF222632)
val SurfaceCard = Color(0xFF151515)
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFFA0A0A0)
val SamsungBlue = Color(0xFF3B82F6)
val DangerRed = Color(0xFFEF4444)
val HighRiskRed = Color(0xFFFF3B30)
val MediumRiskYellow = Color(0xFFFFCC00)
val LowRiskBlue = Color(0xFF32ADE6)
val NoRiskGreen = Color(0xFF34C759)

sealed class Screen {
    object Dashboard : Screen()
    data class AppList(val initialTab: Int = 0) : Screen()
    data class AppDetail(val app: AppRiskInfo) : Screen()
    object Education : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = DarkBackground)) {
                DeadLockApp()
            }
        }
    }
}

@Composable
fun DeadLockApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (currentScreen !is Screen.AppDetail) {
                NavigationBar(containerColor = SurfaceCard, contentColor = TextPrimary) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                        label = { Text("Scanner") },
                        selected = currentScreen is Screen.Dashboard || currentScreen is Screen.AppList,
                        onClick = { currentScreen = Screen.Dashboard },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SamsungBlue,
                            indicatorColor = DarkBackground
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                        label = { Text("Privacy 101") },
                        selected = currentScreen is Screen.Education,
                        onClick = { currentScreen = Screen.Education },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SamsungBlue,
                            indicatorColor = DarkBackground
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val screen = currentScreen) {
                is Screen.Dashboard -> DashboardScreen(
                    onNavigateToList = { tabIndex ->
                        currentScreen = Screen.AppList(tabIndex)
                    }
                )
                is Screen.AppList -> AppListScreen(
                    initialTab = screen.initialTab,
                    onBackClick = { currentScreen = Screen.Dashboard },
                    onAppClick = { app ->
                        currentScreen = Screen.AppDetail(app)
                    }
                )
                is Screen.AppDetail -> AppDetailScreen(
                    app = screen.app, 
                    onBackClick = { currentScreen = Screen.AppList() }
                )
                is Screen.Education -> EducationScreen()
            }
        }
    }
}
