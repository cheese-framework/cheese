package net.codeocean.cheese.core.api

import android.content.Context

interface APP {

    fun getForegroundPkg(): String
    fun openUrl(url: String)
    fun uninstall(packageName: String)
    fun getPackageName(appName: String): String?
    fun getAppName(packageName: String): String?
    fun openAppSettings(packageName: String): Boolean
    fun openApp(packageName: String): Boolean
    fun openScheme(schemeUri: String): Boolean
    fun getApkSha256(filePath: String): String
}