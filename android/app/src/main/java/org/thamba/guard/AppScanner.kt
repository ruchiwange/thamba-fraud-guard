package org.thamba.guard

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/** App guard: looks through the installed apps for the kinds fraudsters ask people to install. */
object AppScanner {

    data class Finding(val packageName: String, val label: String, val level: String, val reason: String)

    fun scan(context: Context): List<Finding> {
        val pm = context.packageManager
        val findings = mutableListOf<Finding>()
        val packages = try {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        } catch (e: Exception) {
            return findings
        }

        for (pkg in packages) {
            // Thamba itself reads SMS and is installed outside a store during development.
            if (pkg.packageName == context.packageName) continue
            val info = pkg.applicationInfo ?: continue
            val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val permissions = pkg.requestedPermissions?.joinToString(",") ?: ""

            val verdict = try {
                Engine.onApp(context, pkg.packageName, installerOf(pm, pkg.packageName), permissions, isSystem)
            } catch (e: Exception) {
                continue
            }
            val level = verdict.optString("level")
            if (level != "none") {
                findings += Finding(pkg.packageName, pm.getApplicationLabel(info).toString(), level, verdict.optString("reason"))
            }
        }
        // Red findings first.
        return findings.sortedBy { if (it.level == "red") 0 else 1 }
    }

    private fun installerOf(pm: PackageManager, packageName: String): String =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        } ?: ""
}
