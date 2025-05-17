package net.codeocean.cheese.core.service


import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hjq.toast.Toaster
import net.codeocean.cheese.core.CoreEnv

import net.codeocean.cheese.core.Misc.launchWithExpHandler
import net.codeocean.cheese.core.R

class CaptureForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopSelf()
        }
    }

    private val channelId by lazy {
        val id = "CaptureForegroundService"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(
                id,
                getString(R.string.cap_fore_service),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }
        id
    }

    private fun getNotification() = NotificationCompat.Builder(this, channelId).apply {
        setContentTitle(getString(R.string.cap_fore_service))
        setContentText("✨✨✨")
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setSmallIcon(android.R.drawable.ic_menu_view)
        setOngoing(true)
    }.build()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                getNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, getNotification())
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mediaProjectionData = intent?.getParcelableExtra<Intent>("mediaProjectionData")
        mediaProjectionData?.let {
            val mediaProjectionManager = CoreEnv.recordScreen.mediaProjectionManager
            val mediaProjection = mediaProjectionManager?.getMediaProjection(Activity.RESULT_OK, it)
            CoreEnv.recordScreen.mediaProjection = mediaProjection
        }
        intent?.action?.also {
            parseAction(it)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun parseAction(action: String) {
        when (action) {
            ACTION_PRINT_LAYOUT -> {
                launchWithExpHandler {
                    Toaster.show("嗯,它看起来一切正常。")
                }
            }
        }

    }

    companion object {
        const val ACTION_PRINT_LAYOUT = "print_layout"
        const val NOTIFICATION_ID = 1

    }
}