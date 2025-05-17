package net.codeocean.cheese.core.api

import android.graphics.Bitmap

interface RecordScreen {
    fun requestPermission(timeout: Int): Boolean
    fun checkPermission(): Boolean
    fun captureScreen(timeout: Int, x: Int, y: Int, ex: Int, ey: Int): Bitmap?
}