package net.codeocean.cheese.backend.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.elvishew.xlog.XLog
import com.hjq.toast.Toaster
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.CoreEnv

import net.codeocean.cheese.core.IAction
import net.codeocean.cheese.core.Misc
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.Script
import net.codeocean.cheese.core.activity.BitmapDisplayActivity
import net.codeocean.cheese.core.api.Base

import net.codeocean.cheese.core.exception.ScriptInterruptedException

object BaseImpl : Base, BaseEnv {
    override fun sleep(tim: Long) {
        try {
            Thread.sleep(tim)
        } catch (e: InterruptedException) {
            throw ScriptInterruptedException()
        }
    }

    override fun setContentView(view: View) {
//        runOnUi {
//            BitmapDisplayActivity.launch(cx, view)
//        }
    }



    override fun exit() {
        CoreEnv.globalVM.get<Script>()?.exit()

    }

    override fun runOnUi(action: IAction) =
        Misc.runOnUi {
            action.invoke()
        }

    override fun release(resource: Any): Boolean {
        return Misc.release(resource)
    }

    override fun Rect(left: Int, top: Int, right: Int, bottom: Int): android.graphics.Rect {
        return android.graphics.Rect(left, top, right, bottom)
    }


    override fun toast(message: String) {
        Toaster.show(message)
    }

    override fun toast(format: String, vararg objects: Any) {
        val msg = String.format(format, *objects)
        Toaster.show(msg)
    }

    override fun toast(message: String, gravity: Int, xOffset: Int, yOffset: Int) {
        Toaster.setGravity(gravity, xOffset, yOffset)
        Toaster.show(message)
    }


}