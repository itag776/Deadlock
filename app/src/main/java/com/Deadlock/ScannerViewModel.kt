@file:Suppress("PackageName")
package com.Deadlock

import android.app.Application
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
    private var cachedApps: List<AppRiskInfo>? = null

    init {
        startAudit()
    }

    fun startAudit(refresh: Boolean = false) {
        // Use cache if available and not refreshing
        if (!refresh && cachedApps != null) {
            uiState = ScannerUiState.Success(cachedApps!!)
            return
        }

        if (uiState is ScannerUiState.Loading) return

        uiState = ScannerUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = scanner.scanApps()
                cachedApps = results

                withContext(Dispatchers.Main) {
                    uiState = if (results.isNotEmpty()) {
                        ScannerUiState.Success(results)
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
