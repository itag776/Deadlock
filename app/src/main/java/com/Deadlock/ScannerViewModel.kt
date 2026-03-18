@file:Suppress("PackageName")
package com.Deadlock

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ScannerUiState {
    object Idle : ScannerUiState
    object Loading : ScannerUiState
    data class Success(val apps: List<AppRiskInfo>) : ScannerUiState
    object Empty : ScannerUiState
    data class Error(val message: String) : ScannerUiState
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<ScannerUiState>(ScannerUiState.Idle)
        private set

    private val scanner = PermissionScanner(application)
    private val packageManager = application.packageManager
    private var cachedApps: List<AppRiskInfo>? = null

    init {
        startAudit()
    }

    fun startAudit(refresh: Boolean = false) {
        if (!refresh && cachedApps != null) {
            uiState = ScannerUiState.Success(cachedApps!!)
            return
        }

        if (uiState is ScannerUiState.Loading) return

        uiState = ScannerUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allApps = scanner.scanApps()
                Log.d("SCAN_DEBUG", "Total scanned: ${allApps.size}")

                // PHASE 3: Correct filtering logic
                val visibleApps = allApps.filter { app ->
                    val appInfo = app.packageInfo.applicationInfo ?: return@filter false
                    
                    val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    val hasLauncher = packageManager.getLaunchIntentForPackage(app.packageName) != null
                    
                    isUserApp || hasLauncher
                }
                
                Log.d("SCAN_DEBUG", "Visible apps: ${visibleApps.size}")
                cachedApps = visibleApps

                withContext(Dispatchers.Main) {
                    uiState = if (visibleApps.isNotEmpty()) {
                        ScannerUiState.Success(visibleApps)
                    } else {
                        ScannerUiState.Empty
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = ScannerUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }
}
