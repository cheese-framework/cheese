package net.codeocean.cheese.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventsUtils() : BroadcastReceiver() {

    private var eventCallback: ((String) -> Unit)? = null

    constructor(eventCallback: (String) -> Unit) : this() {
        this.eventCallback = eventCallback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                when (intent.getStringExtra("reason")) {
                    "homekey" -> {
                        eventCallback?.let { it("home") }
                    }

                    "recentapps" -> {
                        eventCallback?.let { it("recent") }
                    }

                    else -> {
                        eventCallback?.let { it("system_dialog_closed") }
                    }
                }
            }

            "android.media.VOLUME_CHANGED_ACTION" -> {
                val currentVolume =
                    intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)
                val previousVolume =
                    intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1)

                when {
                    currentVolume > previousVolume -> {
                        eventCallback?.let { it("volume_up") }
                    }

                    currentVolume < previousVolume -> {
                        eventCallback?.let { it("volume_down") }
                    }

                    else -> {
                        eventCallback?.let { it("volume_same") }
                    }
                }
            }
        }
    }
}