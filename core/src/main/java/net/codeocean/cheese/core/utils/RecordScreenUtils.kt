package net.codeocean.cheese.core.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.CoreEnv

import net.codeocean.cheese.core.utils.PermissionsUtils.RECORDSCREEN
import java.nio.ByteBuffer


object RecordScreenUtils {
    private var imageReader: ImageReader? = null
    private var resetData: Boolean = false
    private var bitmap: Bitmap? = null

    fun requestPermission(timeout: Int): Boolean {
        return PermissionsUtils.requestPermission(RECORDSCREEN, timeout)
    }


    fun checkPermission(): Boolean {
        return PermissionsUtils.checkPermission(RECORDSCREEN)
    }

    fun resetVirtualDisplay() {
        bitmap?.recycle()
        bitmap=null
        release()
        initializeImageReader()
        setUpVirtualDisplay {}
    }


    fun captureScreen(timeout: Int, left: Int, top: Int, right: Int, bottom: Int): Bitmap? {
        if (!resetData || (imageReader?.width ?: 0) != ScreenUtils.getScreenWidth()) {
            resetVirtualDisplay()
            resetData = true
        }
        val startTime = System.currentTimeMillis()
        val duration = timeout * 1000
        while (System.currentTimeMillis() - startTime < duration) {
            bitmap?.let {
                if (!it.isRecycled) {
                    CoreEnv.recordScreen.bitmap =  Bitmap.createBitmap(it)
                    CoreEnv.recordScreen.bitmap?.let { copy ->
                        return cropBitmap(copy, left, top, right, bottom)
                    }
                }
                return null
            }
        }
        XLog.e("Bitmap Status", "bitmap is null or recycled")
        return null
    }

    private fun cropBitmap(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int): Bitmap {
        val cropRect = Rect(left, top, right, bottom)
        if (left < 0 || top < 0 || right < 0 || bottom < 0) {
          return bitmap
        }
        return if (cropRect.width() > 0 && cropRect.height() > 0) {
            Bitmap.createBitmap(
                bitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
            )
        } else {
            bitmap
        }
    }

    private fun initializeImageReader() {
        imageReader?.surface?.release()
        imageReader?.close()
        imageReader = ImageReader.newInstance(
            ScreenUtils.getScreenWidth(),
            ScreenUtils.getScreenHeight(),
            PixelFormat.RGBA_8888,
            2
        )
    }

    private fun release() {
        CoreEnv.recordScreen.virtualDisplay?.release()
        imageReader?.close()
        imageReader = null
    }
    val mediaProjectionCallback = object : MediaProjection.Callback() {}

    private fun setUpVirtualDisplay(callback: (Boolean) -> Unit) {
        CoreEnv.recordScreen.mediaProjection!!.registerCallback(mediaProjectionCallback, Handler(Looper.getMainLooper()))
        CoreEnv.recordScreen.virtualDisplay = CoreEnv.recordScreen.mediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            ScreenUtils.getScreenWidth(),
            ScreenUtils.getScreenHeight(),
            ScreenUtils.getScreenDensityDpi(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            processImage(image, callback)
        }, Handler(Looper.getMainLooper()))
    }

    private fun updateBitmap(newBitmap: Bitmap?) {
        if (newBitmap != null && bitmap !== newBitmap) {
            bitmap?.let {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
            bitmap = newBitmap
        }
    }


    private fun processImage(image: Image?, callback: (Boolean) -> Unit) {
        if (image == null) {
            XLog.d("Image Status", "image == null")
            callback(false)
            return
        }

        try {
            bitmap = saveImage(image)
            bitmap?.let { updateBitmap(it) }
            callback(bitmap != null)
        } catch (e: Exception) {
            Log.e("Image Processing Error", e.message.toString(), e)
            callback(false)
        } finally {
            image.close()
        }
    }

    private fun saveImage(image: Image): Bitmap? {
        return try {
            val planes = image.planes
            if (planes.isEmpty()) return null
            val width = image.width
            val height = image.height
            val buffer: ByteBuffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val bitmapWidth = width + if (pixelStride > 1) rowPadding / pixelStride else 0
            val bitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            if (bitmap.isRecycled) null else bitmap
        } catch (e: Exception) {
            XLog.e("保存图像时出错: ${e.message}", e)
            null
        } finally {
            image.close()
        }
    }

    private object ScreenUtils {
        fun getScreenWidth(): Int = ScreenManager.getAutoScreenWidth()
        fun getScreenHeight(): Int = ScreenManager.getAutoScreenHeight()
        fun getScreenDensityDpi(): Int = Resources.getSystem().displayMetrics.densityDpi
    }
}
