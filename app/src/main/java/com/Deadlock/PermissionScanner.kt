package com.Deadlock

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class AppRiskInfo(
    val appName: String,
    val packageName: String,
    val hasCamera: Boolean,
    val hasMic: Boolean,
    val hasLocation: Boolean,
    val riskScore: Int
)

class PermissionScanner(private val context: Context) {

    fun scanApps(): List<AppRiskInfo> {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS

        // Fix for Android 13+ (Your Samsung Device)
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            pm.getInstalledPackages(flags)
        }

        val riskyApps = mutableListOf<AppRiskInfo>()

        for (pack in packages) {
            // Filter out system apps
            val isSystemApp = (pack.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
            if (isSystemApp) continue

            // If requestedPermissions is null, skip
            val requestedPermissions = pack.requestedPermissions ?: continue

            val hasCamera = requestedPermissions.contains("android.permission.CAMERA")
            val hasMic = requestedPermissions.contains("android.permission.RECORD_AUDIO")
            val hasLocation = requestedPermissions.contains("android.permission.ACCESS_FINE_LOCATION") ||
                    requestedPermissions.contains("android.permission.ACCESS_COARSE_LOCATION")

            if (hasCamera || hasMic || hasLocation) {
                var score = 0
                if (hasCamera) score++
                if (hasMic) score++
                if (hasLocation) score++

                val appName = pack.applicationInfo?.loadLabel(pm).toString()

                riskyApps.add(
                    AppRiskInfo(
                        appName = appName,
                        packageName = pack.packageName,
                        hasCamera = hasCamera,
                        hasMic = hasMic,
                        hasLocation = hasLocation,
                        riskScore = score
                    )
                )
            }
        }
        return riskyApps.sortedByDescending { it.riskScore }
    }
}