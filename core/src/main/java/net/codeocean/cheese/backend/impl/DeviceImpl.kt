package net.codeocean.cheese.backend.impl

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.github.gzuliyujiang.oaid.IGetter
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.api.Device
import net.codeocean.cheese.core.utils.ScreenManager
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileReader
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object DeviceImpl: Device, BaseEnv {

    override fun getIMEI(): String {
        return DeviceIdentifier.getIMEI(cx)
    }

    override fun getCpuArchitecture(): String {
        return Runtime.getRuntime().exec("getprop ro.product.cpu.abi").inputStream.bufferedReader().readText().trim()
    }

    override fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = cx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            (level * 100 / scale.toFloat()).toInt()
        } ?: -1
    }

    override fun getCpuModel(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            val cpuInfo = reader.readLines()
            reader.close()

            // 查找包含 "model name" 的行
            val modelLine = cpuInfo.find { it.contains("model name") }
            modelLine?.split(":")?.get(1)?.trim() ?: "Unknown CPU model"
        } catch (e: Exception) {
            "Unable to retrieve CPU model"
        }
    }

    override fun getDeviceName(): String {
        return Build.MODEL ?: "Unknown"
    }

    override fun getAppMD5(): String? {
        // 获取当前应用的 APK 路径
        val packageName = cx.packageName
        val packageInfo = cx.packageManager.getPackageInfo(packageName, 0)
        val apkPath = packageInfo.applicationInfo!!.sourceDir

        // 计算 APK 文件的 MD5 值
        return getFileMD5(apkPath)
    }
    private fun getFileMD5(filePath: String): String? {
        try {
            // 创建一个 MessageDigest 实例，使用 MD5 算法
            val digest = MessageDigest.getInstance("MD5")

            // 打开文件输入流
            val fis = FileInputStream(filePath)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            // 读取文件并更新 MD5 计算
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }

            fis.close()

            // 获取计算出的 MD5 值
            val md5Bytes = digest.digest()

            // 将 MD5 值转换成十六进制字符串
            val stringBuilder = StringBuilder()
            for (byte in md5Bytes) {
                stringBuilder.append(String.format("%02x", byte))
            }

            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun supportedOAID(): Boolean {
        return DeviceID.supportedOAID(cx)
    }

    override fun getOAID(): String? {
        val latch = CountDownLatch(1)
        var oaidResult: String? = null
        var oaidError: Exception? = null

        // 调用异步方法，使用 CountDownLatch 等待结果
        com.github.gzuliyujiang.oaid.DeviceID.getOAID(cx, object : IGetter {
            override fun onOAIDGetComplete(result: String) {
                oaidResult = result
                latch.countDown()
            }

            override fun onOAIDGetError(error: Exception) {
                oaidError = error
                latch.countDown()
            }
        })

        // 等待异步操作完成
        latch.await()

        // 检查是否有异常
        if (oaidError != null) {
            throw oaidError!!
        }

        return oaidResult
    }

    internal class LocationUtil(private val context: Context) {
        private val locationManager: LocationManager? =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        private var locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                lastLocation = location
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }
        }
        var lastLocation: android.location.Location? = null

        fun checkPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermission(requestCode: Int) {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        }

        val isGpsEnabled: Boolean
            get() = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false

        fun start() {
            if (checkPermission() && isGpsEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0f,
                    locationListener
                )
                lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
        }

        fun stop() {
            locationManager?.removeUpdates(locationListener)
        }
    }


    override fun getPosition(): String? {
        val latch = CountDownLatch(1)
        var locationResult: String? = null

        // 在UI线程中执行异步操作
        runOnUi {
            val locationUtil = LocationUtil(cx)
            if (!locationUtil.checkPermission()) {
                locationUtil.requestPermission(100)
                latch.countDown() // 确保 CountDownLatch 被递减
                return@runOnUi
            } else {
                if (!locationUtil.isGpsEnabled) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    cx.startActivity(intent)
                    latch.countDown()
                    return@runOnUi
                }
            }
            locationUtil.start()

            // 获取位置的异步操作
            val location: android.location.Location? = locationUtil.lastLocation
            if (location != null) {
                val longitude = location.longitude
                val latitude = location.latitude
                locationResult = "$longitude,$latitude"
            }

            locationUtil.stop()
            latch.countDown() // 确保 CountDownLatch 被递减
        }

        // 等待 3 秒，如果超时，则返回 null
        val isSuccess = latch.await(3, TimeUnit.SECONDS)
        return if (isSuccess) locationResult else null
    }

    override fun getPublicIP(url: String): String {
        val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(10, TimeUnit.SECONDS)     // 读取超时
            .writeTimeout(10, TimeUnit.SECONDS)    // 写入超时
            .build()
        return try {
            val request = Request.Builder()
                .url(url)
                .build()
            client.newCall(request).execute().use { response ->
                return response.body?.string() ?: ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    override fun getWifiIP(): String {
        val wifiManager = cx.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networks = wifiManager.connectionInfo
        val ipInt = networks.ipAddress
        val ip = String.format(
            Locale.US, "%d.%d.%d.%d",
            ipInt and 0xff,
            ipInt shr 8 and 0xff,
            ipInt shr 16 and 0xff,
            ipInt shr 24 and 0xff
        )
        return ip
    }

    override fun getAndroidVersion(): String {
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return "Android $release (API level $sdkVersion)"
    }

    override fun getStatusBarHeight(): Int {
        val resourceId = cx.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            cx.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    override fun getNavigationBarHeight(): Int {
        if (hasNavigationBar(cx)) {
            val resourceId =
                cx.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            // 如果找到了对应的资源标识符，则返回导航栏高度的像素值
            if (resourceId > 0) {
                return cx.resources.getDimensionPixelSize(resourceId)
            }
        }
        return 0
    }

    private fun hasNavigationBar(context: Context): Boolean {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val realSize = Point()
        val screenSize = Point()
        display.getRealSize(realSize)
        display.getSize(screenSize)
        return realSize.y != screenSize.y
    }

    override fun getScreenHeight(): Int {
        return ScreenManager.getAutoScreenHeight()
    }

    override fun getScreenWidth(): Int {
        return ScreenManager.getAutoScreenWidth()
    }

    override fun isLandscape(): Boolean {
        return ScreenManager.isLandscape()
    }

    override fun getScreenDpi(): Int {
        return Resources.getSystem().displayMetrics.densityDpi
    }

    override fun getTime(): Long {
        return System.currentTimeMillis()
    }

    override fun getClipboard(): String? {
        val clipboard = cx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 检查剪贴板是否为空
        if (!clipboard.hasPrimaryClip()) {
            return null
        }
        val clipData = clipboard.primaryClip

        // 检查剪贴板数据项是否为空
        if (clipData == null || clipData.itemCount == 0) {
            return null
        }
        val item = clipData.getItemAt(0)
        val text = item.text ?: return null

        // 检查剪贴板文本是否为空
        return text.toString()
    }

    override fun setClipboard(text: String): Boolean {
        return try {
            val clipboard =
                cx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // 创建ClipData对象，并指定MIME类型为普通文本
            val clipData = ClipData.newPlainText("text", text)

            // 将ClipData对象放入剪贴板
            clipboard.setPrimaryClip(clipData)
            true // 写入剪贴板成功
        } catch (e: Exception) {
            e.printStackTrace()
            false // 写入剪贴板失败
        }
    }

    override val cx: Context
        get() = CoreEnv.envContext.context
}