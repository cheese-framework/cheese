package net.codeocean.cheese.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

import com.hjq.toast.Toaster
import net.codeocean.cheese.core.Misc.launchWithExpHandler
import net.codeocean.cheese.core.R


class ForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopSelf()
        }
    }

    private val channelId by lazy {
        val id = "ForegroundService"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(
                id,
                getString(R.string.fore_service),
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
        setContentTitle(getString(R.string.fore_service))
        setContentText("✨✨✨")
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setSmallIcon(android.R.drawable.ic_menu_myplaces)
        setOngoing(true)
    }.build()

    override fun onCreate() {
        super.onCreate()
        startForeground(1999, getNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

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
    }
}