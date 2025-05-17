package net.codeocean.cheese.core.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import cn.vove7.andro_accessibility_api.AccessibilityApi

import com.elvishew.xlog.XLog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.Misc.isForegroundServiceRunning
import net.codeocean.cheese.core.activity.RecordScreenActivity
import net.codeocean.cheese.core.service.CaptureForegroundService
import java.io.File
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object PermissionsUtils {
    private val mediaProjectionManager: MediaProjectionManager =
        CoreEnv.envContext.context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    const val ACCESSIBILITY = 1
    const val FLOATING = 2
    const val RECORDSCREEN = 3
    const val ROOT = 4


    fun Activity.initPermissions(block: () -> Unit) {
        val context = this
        val blockCalled = AtomicBoolean(false)
        ScreenManager.init(this)

        fun safeCallBlock() {
            if (blockCalled.compareAndSet(false, true)) {
                thread { block() }

            }
        }
        XXPermissions.with(this)
            .permission(Permission.REQUEST_INSTALL_PACKAGES)
            .permission(Permission.POST_NOTIFICATIONS)
            .permission(Permission.GET_INSTALLED_APPS)
            .permission(Permission.SCHEDULE_EXACT_ALARM)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .permission(Permission.ACCESS_FINE_LOCATION)
            .permission(Permission.READ_PHONE_STATE)
            .permission(Permission.SYSTEM_ALERT_WINDOW)
            .permission(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .request(object : OnPermissionCallback {
                @Synchronized
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        Toaster.show("获取部分权限成功，但部分权限未正常授予")
                    } else {
                        Toaster.show("所需权限正常")
                    }
                    safeCallBlock()
                }

                @Synchronized
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        Toaster.show("被永久拒绝授权，请手动授予权限")
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        Toaster.show("获取所需权限失败")
                    }
                    safeCallBlock()
                }
            })
    }

    fun requestPermission(permission: Int, timeout: Int): Boolean {
        return when (permission) {
            ACCESSIBILITY -> requestAccessibilityPermission(timeout)
            FLOATING -> requestFloatingPermission(timeout)
            RECORDSCREEN -> requestRecordScreenPermission(timeout)
            ROOT -> requestRootPermission(timeout)
            else -> false
        }
    }

    fun checkPermission(permission: Int): Boolean {
        return when (permission) {
            ACCESSIBILITY -> checkAccessibilityPermission()
            FLOATING -> checkFloatingPermission()
            RECORDSCREEN -> checkRecordScreenPermission()
            ROOT -> checkRootPermission()
            else -> {
                XLog.e("未知权限码")
                false
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun screenRecord(resultCode: Int, data: Intent?): Boolean {
        if (resultCode != Activity.RESULT_OK) {
            Log.d("~~~", "User cancelled")
            return false
        }
        Log.d("~~~", "Starting screen capture")
        if (!isForegroundServiceRunning(CaptureForegroundService.NOTIFICATION_ID)) {

            val intent = Intent(
                CoreEnv.envContext.context,
                CaptureForegroundService::class.java
            )
            intent.putExtra("mediaProjectionData", data)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CoreEnv.envContext.context.startForegroundService(
                    intent
                )
            } else {
                CoreEnv.envContext.context.startService(
                    intent
                )
            }
        }
        sleep(500)
        return true
    }

    private fun requestFloatingPermission(timeout: Int): Boolean {
        CoreEnv.envContext.activity?.let {
            if (checkFloatingPermission()) {
                return true
            }
            if (!Settings.canDrawOverlays(CoreEnv.envContext.activity)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:" + it.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.startActivity(intent)
            }
            for (i in 0 until timeout * 10) {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                if (checkFloatingPermission()) {
                    return true
                }
            }
        }


        return false
    }

    private fun checkFloatingPermission(): Boolean {
        return Settings.canDrawOverlays(CoreEnv.envContext.context)
    }

    private fun requestRootPermission(timeout: Int): Boolean {
        return try {
            val cmd = "chmod 777 ${CoreEnv.envContext.context.packageCodePath}"
            Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).apply {
                outputStream.bufferedWriter().use { it.write("exit\n") }
                waitFor()
            }.exitValue()
            for (i in 0 until timeout * 10) {
                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                if (checkRootPermission()) {
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            false
        }
    }

    private fun checkRootPermission(): Boolean {
        return try {
            val file1 = File("/system/bin/su")
            val file2 = File("/system/xbin/su")
            (file1.exists() && file1.canExecute()) || (file2.exists() && file2.canExecute())
        } catch (e: Exception) {
            false
        }
    }


    private fun requestAccessibilityPermission(timeout: Int): Boolean {
        if (checkAccessibilityPermission()) {
            return true
        }
        cn.vove7.auto.core.utils.jumpAccessibilityServiceSettings(AccessibilityApi.BASE_SERVICE_CLS)
        for (i in 0 until timeout * 10) {
            try {
                sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            if (checkAccessibilityPermission()) {
                return true
            }
        }
        return false

    }

    private fun checkAccessibilityPermission(): Boolean {
        val accessibilityManager =
            CoreEnv.envContext.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val packageName = CoreEnv.envContext.context.packageName
        var isServiceEnabled = false
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == packageName) {
                isServiceEnabled = true
                break
            }
        }

        return isServiceEnabled
    }

    private fun requestRecordScreenPermission(timeout: Int): Boolean {

        CoreEnv.recordScreen.mediaProjectionManager = this.mediaProjectionManager

        if (checkRecordScreenPermission()) {
            return true
        }
        val intent = Intent(CoreEnv.envContext.context, RecordScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        CoreEnv.envContext.context.startActivity(intent)
        sleep(500)
        Log.d("~~~", "Requesting confirmation")
        for (i in 0 until timeout * 10) {
            try {
                sleep(100)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            if (checkRecordScreenPermission()) {
                return true
            }
        }
        return false
    }

    private fun checkRecordScreenPermission(): Boolean {
        return (CoreEnv.recordScreen.mediaProjection != null && isForegroundServiceRunning(
            CaptureForegroundService.NOTIFICATION_ID
        ))
    }


}