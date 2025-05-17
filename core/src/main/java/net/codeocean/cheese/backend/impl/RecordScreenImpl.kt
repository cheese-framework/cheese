package net.codeocean.cheese.backend.impl

import android.graphics.Bitmap
import net.codeocean.cheese.core.api.RecordScreen
import net.codeocean.cheese.core.utils.RecordScreenUtils

object RecordScreenImpl: RecordScreen {
    override fun requestPermission(timeout: Int): Boolean {
       return RecordScreenUtils.requestPermission(timeout)
    }

    override fun checkPermission(): Boolean {
        return RecordScreenUtils.checkPermission()
    }

    override fun captureScreen(timeout: Int, x: Int, y: Int, ex: Int, ey: Int): Bitmap? {
        return RecordScreenUtils.captureScreen(timeout,x,y,ex,ey)
    }
}