package net.codeocean.cheese.backend.impl

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings


import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.api.APP
import net.codeocean.cheese.core.service.accessibilityEvent
import net.codeocean.cheese.core.service.foregroundPkg
import java.io.BufferedInputStream
import java.io.File
import java.security.MessageDigest


object APPImpl : APP, BaseEnv {

    override fun getForegroundPkg(): String {
        return foregroundPkg.toString()
    }

    override fun openUrl(url: String) {
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
        } else {
            url
        }
        cx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun uninstall(packageName: String) {
        cx.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun getPackageName(appName: String): String? {
        val packageManager = cx.applicationContext.packageManager
        val installedApplications =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (applicationInfo in installedApplications) {
            if (packageManager.getApplicationLabel(applicationInfo).toString() == appName) {
                return applicationInfo.packageName
            }
        }
        return null
    }

    override fun getAppName(packageName: String): String? {
        val packageManager = cx.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo)
            appName.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun openAppSettings(packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            cx.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    override fun openApp(packageName: String): Boolean {
        val packageManager = cx.applicationContext.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            cx.startActivity(intent)
            true
        } else {
            false
        }
    }

    override fun openScheme(schemeUri: String): Boolean {
        if (schemeUri.isEmpty()) {
            return false
        }
        val uri = Uri.parse(schemeUri) ?: return false
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return if (intent.resolveActivity(cx.packageManager) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            cx.startActivity(intent)
            true
        } else {
            false
        }
    }

    override fun getApkSha256(filePath: String): String {
        val file = File(filePath)
        val buffer = ByteArray(8192)
        val digest = MessageDigest.getInstance("SHA-256")

        BufferedInputStream(file.inputStream()).use { input ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    override val cx: Context
        get() = CoreEnv.envContext.context
}