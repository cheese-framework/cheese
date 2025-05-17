package net.codeocean.cheese.core.window

import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.hjq.window.EasyWindow
import net.codeocean.cheese.core.CoreEnv

import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

class Logcat(private val baseEnv: CoreEnv.EnvContext, private val console: Console?) {
    private lateinit var textView: AppCompatTextView
    private lateinit var view: View
    private lateinit var wm: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var isConsoleVisible = false
    private var isConsoleHide = false
    private var logCounter = 1

    // 记录初始窗口位置
    private var initialX = 0
    private var initialY = 0

    // 记录初始触摸点的位置
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var filePathShow = false

    // 显示控制台窗口
    fun show() {
        if (isConsoleHide) {
            runOnUi {
                view.visibility = View.VISIBLE
            }
            isConsoleHide = false
        } else {
            if (isConsoleVisible) return
            runOnUi {
                EasyWindow.with(baseEnv.activity).apply {
                    setContentView(R.layout.logcat)
                    setGravity(Gravity.CENTER)
                    configureWindowDimensions()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                    } else {
                        setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                    }
                    setWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    setBitmapFormat(PixelFormat.TRANSLUCENT)
                    textView =
                        findViewById<AppCompatTextView>(R.id.tv_content) as AppCompatTextView
                    textView.movementMethod = ScrollingMovementMethod.getInstance()

                    textView.textSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        3.5f,
                        baseEnv.context.resources.displayMetrics
                    )
                    textView.setTextColor(Color.WHITE)
                    textView.setBackgroundColor(Color.argb(0x55, 0x00, 0x00, 0x00))

                    val dragView = findViewById<View>(R.id.layout_drag)

                    layoutParams = this.windowParams
                    dragView.setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                initialX = layoutParams.x
                                initialY = layoutParams.y
                                initialTouchX = event.rawX
                                initialTouchY = event.rawY
                                true
                            }

                            MotionEvent.ACTION_MOVE -> {
                                layoutParams.x =
                                    (initialX + (event.rawX - initialTouchX)).roundToInt()
                                layoutParams.y =
                                    (initialY + (event.rawY - initialTouchY)).roundToInt()

                                // 检查视图是否附加
                                if (view.parent != null) {
                                    wm.updateViewLayout(view, layoutParams)
                                }
                                true
                            }

                            MotionEvent.ACTION_UP -> {
                                if (event.rawX == initialTouchX && event.rawY == initialTouchY) {
                                    dragView.performClick()
                                }
                                true
                            }

                            else -> false
                        }
                    }
                    setOnClickListener(R.id.iv_close) { easyWindow, _ ->
                        hideConsole(easyWindow)
                        console?.logcatButton?.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                    }

                    wm = this.windowManager
                    view = this.contentView.rootView

                }.show()
            }
            isConsoleVisible = true
        }

    }

    fun destroy() {
        if (!isConsoleVisible || view.parent == null) return
        runOnUi {
            wm.removeView(view)
            resetDebugButtonColor()
        }
        isConsoleVisible = false
        logCounter = 1

    }


    fun hide() {
        if (!isConsoleVisible || view.parent == null) return
        runOnUi {
            view.visibility = View.GONE
            resetDebugButtonColor()
        }
        isConsoleHide = true
    }

    fun extractFileName(input: String): String {
        val normalizedPath = input.replace("\\", "/").trim()
        return normalizedPath.substringAfterLast("/")
    }

    fun log(message: String) {
        if (!isConsoleVisible || !::textView.isInitialized) return
        runOnUi {
            textView.apply {
                var msg = message
                if (filePathShow) {
                    msg = extractFileName(message)
                }

                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
                val logLevel = when {
                    msg.contains(" E ") -> "[ERROR]"
                    msg.contains(" W ") -> "[WARN]"
                    msg.contains(" I ") -> "[INFO]"
                    msg.contains(" D ") -> "[DEBUG]"
                    else -> "[OTHER]"
                }
                msg = "$timestamp $logLevel $msg"

                when {
                    msg.contains(" E ") -> { // 错误日志
                        setTextColor(Color.RED)
                        setTypeface(typeface, Typeface.BOLD)
                    }

                    msg.contains(" W ") -> { // 警告日志
                        setTextColor(Color.YELLOW)
                        setTypeface(typeface, Typeface.BOLD)
                    }

                    msg.contains(" I ") -> { // 信息日志
                        setTextColor(Color.BLUE)
                    }

                    msg.contains(" D ") -> { // 调试日志
                        setTextColor(Color.CYAN)
                    }

                    else -> {
                        setTextColor(Color.WHITE)
                    }
                }

                // 显示日志内容
                append("\n$msg\n")

                // 滚动到最新一行
                post { scrollToLatestLine() }
            }
            wm.updateViewLayout(view, layoutParams)
        }
        logCounter++
    }

    fun setTextSize(size: Float) {
        if (!isConsoleVisible || !::textView.isInitialized) return
        runOnUi {
            textView.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                size,
                baseEnv.context.resources.displayMetrics
            )
        }

    }


    fun filePathShow(on: Boolean) {
        filePathShow = on
    }

    fun setXOffset(offset: Int) {
        if (!isConsoleVisible || view.parent == null) return
        runOnUi {
            layoutParams.let { params ->
                params.x = offset
                wm.updateViewLayout(view, params)
            }
        }
    }

    fun setYOffset(offset: Int) {
        if (!isConsoleVisible || view.parent == null) return
        runOnUi {
            layoutParams.let { params ->
                params.y = offset
                wm.updateViewLayout(view, params)
            }
        }
    }

    fun setGravity(gravity: Int) {
        if (!isConsoleVisible || view.parent == null) return
        runOnUi {
            layoutParams.let { params ->
                params.gravity = gravity
                wm.updateViewLayout(view, params)
            }
        }
    }


    fun clear() {
        if (!isConsoleVisible || !::textView.isInitialized) return
        textView.text = ""
        runOnUi {
            wm.updateViewLayout(view, layoutParams)
        }
        logCounter = 1
    }


    fun setTouch(enabled: Boolean) {
        if (!isConsoleVisible || !::layoutParams.isInitialized) return
        layoutParams.flags = if (enabled) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        } else {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        runOnUi {
            wm.updateViewLayout(view, layoutParams)
        }


    }

    private fun AppCompatTextView.scrollToLatestLine() {
        layout?.let {
            val lastLineOffset = it.getLineTop(lineCount) - height
            scrollTo(0, max(lastLineOffset, 0))
        }
    }

    private fun EasyWindow<*>.configureWindowDimensions() {
        val displayMetrics = DisplayMetrics().apply {
            (ContextCompat.getSystemService(
                baseEnv.context,
                WindowManager::class.java
            ))?.defaultDisplay?.getRealMetrics(this)
        }
        setWidth((displayMetrics.widthPixels * 0.55).toInt())
        setHeight((displayMetrics.heightPixels * 0.22).toInt())
    }

    private fun hideConsole(easyWindow: EasyWindow<*>) {
        isConsoleVisible = false
        easyWindow.cancel()
        resetDebugButtonColor()
        logCounter = 1
    }

    private fun resetDebugButtonColor() {
        console?.logcatButton?.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
    }


}