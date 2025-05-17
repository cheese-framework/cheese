package net.codeocean.cheese.core.utils
import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import net.codeocean.cheese.core.CoreEnv




object ScreenManager {
    private var deviceScreenHeight: Int = 0
    private var deviceScreenWidth: Int = 0
    private var initialized: Boolean = false
    private var deviceScreenDensity: Int = 0

    fun init(activity: Activity) {
        if (initialized) return

        val metrics = DisplayMetrics().apply {
            activity.windowManager.defaultDisplay.getRealMetrics(this)
        }

        deviceScreenHeight = metrics.heightPixels.takeIf { it != 0 }
            ?: activity.windowManager.defaultDisplay.height.takeIf { it != 0 }
                    ?: activity.resources.displayMetrics.heightPixels

        deviceScreenWidth = metrics.widthPixels.takeIf { it != 0 }
            ?: activity.windowManager.defaultDisplay.width.takeIf { it != 0 }
                    ?: activity.resources.displayMetrics.widthPixels

        if (deviceScreenWidth > deviceScreenHeight) {
            val temp = deviceScreenWidth
            deviceScreenWidth = deviceScreenHeight
            deviceScreenHeight = temp
        }

        deviceScreenDensity = metrics.densityDpi
        initialized = true
    }

    fun getDeviceScreenHeight() = deviceScreenHeight

    fun getDeviceScreenWidth() = deviceScreenWidth

    fun getDeviceScreenDensity() = deviceScreenDensity

    fun getAutoScreenWidth() = if (isLandscape()) getDeviceScreenHeight() else getDeviceScreenWidth()

    fun getAutoScreenHeight() = if (isLandscape()) getDeviceScreenWidth() else getDeviceScreenHeight()

    private fun scale(value: Int, dimension: Int) =
        if (dimension == 0 || !initialized) value else value * deviceScreenWidth / dimension

    fun scaleX(x: Int, width: Int) = scale(x, width)

    fun scaleY(y: Int, height: Int) = scale(y, height)

    private fun rescale(value: Int, dimension: Int) =
        if (dimension == 0 || !initialized) value else value * dimension / deviceScreenWidth

    fun rescaleX(x: Int, width: Int) = rescale(x, width)

    fun rescaleY(y: Int, height: Int) = rescale(y, height)

    fun isLandscape(): Boolean {
        val windowManager = CoreEnv.envContext.activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation
        return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
    }


}

