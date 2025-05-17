package net.codeocean.cheese.core.api

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import net.codeocean.cheese.core.IAction
import net.codeocean.cheese.core.Script




interface Base {
    fun setContentView(view: View)
    fun sleep(tim: Long)
    fun toast(message: String)
    fun toast(format: String, vararg objects: Any)
    fun toast(message: String, gravity: Int = Gravity.BOTTOM, xOffset: Int = 0, yOffset: Int = 0)
    fun exit()
    fun runOnUi(action: IAction)
    fun release(resource: Any): Boolean
    fun Rect(left: Int, top: Int, right: Int, bottom: Int):android.graphics.Rect


}