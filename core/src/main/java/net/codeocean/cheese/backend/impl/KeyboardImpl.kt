package net.codeocean.cheese.backend.impl

import android.os.SystemClock
import android.view.KeyEvent
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.api.Keyboard

object KeyboardImpl:Keyboard,BaseEnv {
    override fun input(text: String) {
        val currentInputConnection = ims?.getCurrentInputConnection()
        currentInputConnection?.commitText(text, 0)
    }

    override fun delete() {
        try {
            val currentInputConnection = ims?.getCurrentInputConnection()
            currentInputConnection?.deleteSurroundingText(Int.MAX_VALUE, Int.MAX_VALUE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun enter() {
        val currentInputConnection = ims?.getCurrentInputConnection()
        if (currentInputConnection != null) {
            val uptimeMillis = SystemClock.uptimeMillis()
            var keyEvent = KeyEvent(
                uptimeMillis,
                uptimeMillis,
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_ENTER,
                0
            )
            currentInputConnection.sendKeyEvent(keyEvent)
            keyEvent =
                KeyEvent(uptimeMillis, uptimeMillis, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0)
            currentInputConnection.sendKeyEvent(keyEvent)
        }
    }
}