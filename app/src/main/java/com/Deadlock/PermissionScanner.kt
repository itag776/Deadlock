@file:Suppress("PackageName")
package com.Deadlock

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

data class AppRiskInfo(
    val appName: String,
    val packageName: String,
    val riskScore: Int, // 0-10 normalized
    val riskLevel: String, // Low, Medium, High
    val grantedPermissions: List<String>,
    val isRealGranted: Boolean,
    val riskExplanation: String
)

class PermissionScanner(private val context: Context) {

    @SuppressLint("QueryPermissionsNeeded")
    fun scanApps(): List<AppRiskInfo> {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS

        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(flags)
        }

        return packages.map { pack ->
            val appName = pack.applicationInfo?.loadLabel(pm)?.toString() ?: pack.packageName
            val (granted, isReal) = getGrantedPermissions(pack)
            
            // Filter only standard android permissions
            val filteredPermissions = granted.filter { it.startsWith("android.permission.") }
            
            val scoreResult = calculateNormalizedScore(filteredPermissions)

            AppRiskInfo(
                appName = appName,
                packageName = pack.packageName,
                riskScore = scoreResult.score,
                riskLevel = scoreResult.level,
                grantedPermissions = filteredPermissions,
                isRealGranted = isReal,
                riskExplanation = scoreResult.explanation
            )
        }.sortedByDescending { it.riskScore }
    }

    private fun getGrantedPermissions(pack: PackageInfo): Pair<List<String>, Boolean> {
        val requested = pack.requestedPermissions ?: return emptyList<String>() to false
        val flags = pack.requestedPermissionsFlags
        
        if (flags == null) return requested.toList() to false

        val granted = mutableListOf<String>()
        for (i in requested.indices) {
            if ((flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                granted.add(requested[i])
            }
        }
        return granted to true
    }

    private data class ScoreResult(val score: Int, val level: String, val explanation: String)

    private fun calculateNormalizedScore(permissions: List<String>): ScoreResult {
        var totalWeight = 0
        val highRisk = mutableListOf<String>()
        
        for (p in permissions) {
            totalWeight += when (p) {
                "android.permission.CAMERA" -> { highRisk.add("camera"); 3 }
                "android.permission.RECORD_AUDIO" -> { highRisk.add("microphone"); 3 }
                "android.permission.ACCESS_FINE_LOCATION" -> { highRisk.add("location"); 3 }
                "android.permission.READ_SMS", "android.permission.SEND_SMS", "android.permission.RECEIVE_SMS" -> { highRisk.add("SMS"); 3 }
                "android.permission.READ_CONTACTS" -> { highRisk.add("contacts"); 3 }
                "android.permission.READ_CALL_LOG" -> { highRisk.add("call logs"); 3 }
                
                "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" -> 2
                "android.permission.READ_PHONE_STATE" -> 2
                
                "android.permission.INTERNET" -> 1
                "android.permission.ACCESS_NETWORK_STATE" -> 1
                else -> 0
            }
        }

        // Normalize to 10 (arbitrary max weight for normalization, say 15)
        val normalized = ((totalWeight / 15f) * 10).toInt().coerceIn(0, 10)
        
        val level = when {
            normalized >= 7 -> "High"
            normalized >= 4 -> "Medium"
            else -> "Low"
        }

        val explanation = if (highRisk.isNotEmpty()) {
            "This app can access your ${highRisk.distinct().joinToString(" and ")}, which may expose private data."
        } else if (normalized > 0) {
            "This app has access to some device features and connectivity."
        } else {
            "This app follows privacy-first guidelines with minimal access."
        }

        return ScoreResult(normalized, level, explanation)
    }
}
