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
    val riskScore: Int,
    val riskLevel: String,
    val grantedPermissions: List<PermissionDetail>,
    val isRealGranted: Boolean,
    val riskExplanation: String,
    val packageInfo: PackageInfo
)

data class PermissionDetail(
    val rawName: String,
    val readableName: String,
    val severity: String
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

        // PHASE 2: Fetch ALL apps, no filtering here
        return packages.map { pack ->
            val appName = pack.applicationInfo?.loadLabel(pm)?.toString() ?: pack.packageName
            val (granted, isReal) = getGrantedPermissions(pack)
            
            val permissionDetails = granted
                .filter { it.startsWith("android.permission.") }
                .map { mapToPermissionDetail(it) }
                .filter { it.readableName.isNotEmpty() }
                .distinctBy { it.readableName }

            val scoreResult = calculateNormalizedScore(permissionDetails)

            AppRiskInfo(
                appName = appName,
                packageName = pack.packageName,
                riskScore = scoreResult.score,
                riskLevel = scoreResult.level,
                grantedPermissions = permissionDetails,
                isRealGranted = isReal,
                riskExplanation = scoreResult.explanation,
                packageInfo = pack
            )
        }
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

    private fun mapToPermissionDetail(permission: String): PermissionDetail {
        val (readable, severity) = when (permission) {
            "android.permission.CAMERA" -> "Can use your camera" to "Critical"
            "android.permission.RECORD_AUDIO" -> "Can access your microphone" to "Critical"
            "android.permission.ACCESS_FINE_LOCATION" -> "Can track your precise location" to "Critical"
            "android.permission.ACCESS_COARSE_LOCATION" -> "Can track your approximate location" to "Sensitive"
            "android.permission.READ_SMS" -> "Can read your messages" to "Critical"
            "android.permission.SEND_SMS" -> "Can send messages" to "Critical"
            "android.permission.RECEIVE_SMS" -> "Can intercept incoming messages" to "Critical"
            "android.permission.READ_CONTACTS" -> "Can read your contacts" to "Critical"
            "android.permission.READ_CALL_LOG" -> "Can view your call history" to "Critical"
            "android.permission.READ_EXTERNAL_STORAGE", 
            "android.permission.WRITE_EXTERNAL_STORAGE" -> "Can access your files & photos" to "Sensitive"
            "android.permission.READ_PHONE_STATE" -> "Can read phone identity & status" to "Sensitive"
            "android.permission.INTERNET" -> "Has internet access" to "Basic"
            "android.permission.ACCESS_NETWORK_STATE" -> "Can view network connections" to "Basic"
            else -> "" to "Other"
        }
        return PermissionDetail(permission, readable, severity)
    }

    private data class ScoreResult(val score: Int, val level: String, val explanation: String)

    private fun calculateNormalizedScore(details: List<PermissionDetail>): ScoreResult {
        var totalWeight = 0
        val highRiskNames = mutableListOf<String>()
        
        details.forEach { detail ->
            totalWeight += when (detail.severity) {
                "Critical" -> {
                    highRiskNames.add(detail.readableName.lowercase().removePrefix("can "))
                    3
                }
                "Sensitive" -> 2
                "Basic" -> 1
                else -> 0
            }
        }

        val normalized = ((totalWeight / 12f) * 10).toInt().coerceIn(0, 10)
        val level = when {
            normalized >= 7 -> "High"
            normalized >= 4 -> "Medium"
            else -> "Low"
        }

        val explanation = if (highRiskNames.isNotEmpty()) {
            "Allowed to ${highRiskNames.distinct().take(2).joinToString(" and ")}."
        } else {
            "Minimal data access detected."
        }

        return ScoreResult(normalized, level, explanation)
    }
}
