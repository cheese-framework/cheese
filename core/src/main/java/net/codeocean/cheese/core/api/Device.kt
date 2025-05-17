package net.codeocean.cheese.core.api

interface Device {
    fun getIMEI(): String
    fun getCpuArchitecture(): String
    fun getDeviceName(): String
    fun getBatteryLevel(): Int
    fun getCpuModel(): String
    fun getAppMD5(): String?
    fun supportedOAID(): Boolean
    fun getOAID(): String?
    fun getPosition(): String?
    fun getPublicIP(url: String): String
    fun getWifiIP(): String
    fun getAndroidVersion(): String
    fun getStatusBarHeight(): Int
    fun getNavigationBarHeight(): Int
    fun getScreenHeight(): Int
    fun getScreenWidth(): Int
    fun isLandscape(): Boolean
    fun getScreenDpi(): Int
    fun getTime(): Long
    fun getClipboard(): String?
    fun setClipboard(text: String): Boolean
}