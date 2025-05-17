package net.codeocean.cheese.backend.impl

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.hjq.window.EasyWindow
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.api.Canvas

object CanvasImpl :Canvas,BaseEnv {
    private inline fun <reified T> Any.castOrThrow(paramName: String): T {
        return this as? T
            ?: throw IllegalArgumentException("Expected $paramName to be of type ${T::class}, but got ${this::class}")
    }

    override fun drawRectOnScreen(vararg elements: Any): EasyWindow<*>? {
        return when (elements.size) {
            4 -> {
                val textColor = elements[0].castOrThrow<Int>("textColor")
                val borderedColor = elements[1].castOrThrow<Int>("borderedColor")
                val similarityText = elements[2].castOrThrow<String>("similarityText")
                val rect = elements[3].castOrThrow<Rect>("rect")
                drawRectOnScreen(
                    similarityText,
                    textColor,
                    borderedColor,
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom
                )
            }

            2 -> {
                val textColor = Color.RED
                val borderedColor = Color.GREEN
                val similarityText = elements[0].castOrThrow<String>("similarityText")
                val rect = elements[1].castOrThrow<Rect>("rect")
                drawRectOnScreen(
                    similarityText,
                    textColor,
                    borderedColor,
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom
                )
            }

            else -> null
        }


    }

    override fun drawPointOnScreen(vararg elements: Any): EasyWindow<*>? {
        return when (elements.size) {
            5 -> {
                val textColor = elements[0].castOrThrow<Int>("textColor")
                val pointColor = elements[1].castOrThrow<Int>("pointColor")
                val similarityText = elements[2].castOrThrow<String>("similarityText")
                val x = elements[3].castOrThrow<Double>("x").toInt()
                val y = elements[4].castOrThrow<Double>("y").toInt()
                drawPointOnScreen(
                    similarityText,
                    textColor,
                    pointColor,
                    x,
                    y
                )
            }

            3 -> {
                val textColor = Color.RED
                val pointColor = Color.GREEN
                val similarityText = elements[0].castOrThrow<String>("similarityText")
                val x = elements[1].castOrThrow<Double>("x").toInt()
                val y = elements[2].castOrThrow<Double>("y").toInt()
                drawPointOnScreen(
                    similarityText,
                    textColor,
                    pointColor,
                    x,
                    y
                )
            }

            else -> null
        }
    }

    private  fun drawRectOnScreen(similarityText:String,textColor:Int,borderedColor:Int,left:Int,top:Int,right:Int,bottom:Int): EasyWindow<*> {
        return EasyWindow.with(act).apply {
            // 创建根布局，并设置高度为整个屏幕的高度
            val rootLayout = ConstraintLayout(context).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // 创建一个带边框的 View
            val borderedView = object : View(context) {
                // 创建全局的 Paint 对象和 RectF 对象
                private val paint = Paint().apply {
                    color = borderedColor // 边框颜色
                    style = Paint.Style.STROKE
                    strokeWidth = 2f // 边框宽度
                }
                private val borderRect = RectF()
                private val textPaint = Paint().apply {
                    color = textColor
                    textSize = 30f // 文本大小
                }
                override fun onDraw(canvas: android.graphics.Canvas) {
                    super.onDraw(canvas)

                    // 重用 RectF 对象，并设置矩形的位置
                    borderRect.set(
                        left.toFloat(), // left
                        top.toFloat(), // top
                        right.toFloat(), // right
                        bottom.toFloat() // bottom
                    )
                    // 绘制边框
                    canvas.drawRect(borderRect, paint)

                    // 添加相似度标签
                    canvas.drawText(similarityText, left.toFloat(), top.toFloat() - 10f, textPaint) // 调整文本位置
                }
            }

            // 设置视图背景为透明
            borderedView.setBackgroundColor(Color.TRANSPARENT)

            // 将带边框的 View 添加到根布局中
            rootLayout.addView(borderedView)

            // 设置浮动窗口类型和标志
            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val windowFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

            val windowManagerParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                windowFlags,
                PixelFormat.TRANSLUCENT
            )
            setWindowParams(windowManagerParams)

            setBitmapFormat(PixelFormat.TRANSLUCENT)
//            setDuration(time)
            // 将根布局设置为内容视图
            setContentView(rootLayout)

        }
    }

    private fun drawPointOnScreen(
        similarityText: String,
        textColor: Int,
        pointColor: Int,
        x: Int,
        y: Int
    ): EasyWindow<*> {
        return EasyWindow.with(act).apply {
            val rootLayout = ConstraintLayout(context).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val pointView = object : View(context) {
                private val pointPaint = Paint().apply {
                    color = pointColor
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }

                private val textPaint = Paint().apply {
                    color = textColor
                    textSize = 30f
                    isAntiAlias = true
                }

                override fun onDraw(canvas: android.graphics.Canvas) {
                    super.onDraw(canvas)

                    // 绘制点
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 6f, pointPaint)

                    // 绘制文字，稍微偏移避免遮住点
                    canvas.drawText(similarityText, x + 10f, y - 10f, textPaint)
                }
            }

            pointView.setBackgroundColor(Color.TRANSPARENT)
            rootLayout.addView(pointView)

            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val windowFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

            val windowManagerParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                windowFlags,
                PixelFormat.TRANSLUCENT
            )
            setWindowParams(windowManagerParams)

            setBitmapFormat(PixelFormat.TRANSLUCENT)
            setContentView(rootLayout)
        }
    }

}