package net.codeocean.cheese.core.window

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.elvishew.xlog.XLog
import com.petterp.floatingx.FloatingX
import com.petterp.floatingx.assist.FxScopeType

import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.CoreEnv.globalVM
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.R
import net.codeocean.cheese.core.Script
import net.codeocean.cheese.core.runtime.ScriptExecutionController.cancelAndClean
import net.codeocean.cheese.core.runtime.debug.remote.DebugController

class Console(private val baseEnv: CoreEnv.EnvContext) {
    var runAndStopButton: ImageButton? = null
    var logcatButton: ImageButton? = null
    var isStarted = false
    private var logcat: Logcat? = null

    @SuppressLint("ObjectAnimatorBinding")
    fun show() {
        runOnUi {
            if (!FloatingX.isInstalled()) {
                FloatingX.install {
                    setContext(baseEnv.context)
                    setLayout(R.layout.console)
                    setScopeType(FxScopeType.SYSTEM)
                    setEdgeOffset(5f)
                }
            }
            FloatingX.configControl().apply {
                setEnableEdgeRebound(true)
                setEnableAnimation(true)
            }
            val control = FloatingX.controlOrNull()
            control?.let {
                it.show()
                val floatingIcon = it.getView()!!.findViewById<ImageView>(R.id.floatingIcon)
                val menuLayout = it.getView()!!.findViewById<LinearLayout>(R.id.menuLayout1)
                runAndStopButton = it.getView()!!.findViewById<ImageButton>(R.id.runAndStop)
                logcatButton = it.getView()!!.findViewById<ImageButton>(R.id.logcat)
                val x = it.getView()!!.findViewById<ImageButton>(R.id.x)
                menuLayout.visibility = View.GONE
                menuLayout.alpha = 0f
                menuLayout.layoutParams.height = 0
                val animationDuration = 200L
                val handler = Handler(Looper.getMainLooper())
                var lastMoveTime = System.currentTimeMillis()
                var isHalfHideEnabled = false

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastMoveTime >= 3000) {
                            // 两秒未移动，启用半隐藏
                            if (!isHalfHideEnabled) {
                                FloatingX.configControl().setEnableHalfHide(true)
                                isHalfHideEnabled = true
                                val heightAnimator = ObjectAnimator.ofInt(
                                    menuLayout, "layoutParams.height",
                                    menuLayout.height, 0
                                ).apply {
                                    duration = animationDuration
                                    start()
                                }

                                ObjectAnimator.ofFloat(
                                    menuLayout, "alpha", 1f, 0f
                                ).apply {
                                    duration = animationDuration
                                    start()
                                }

                                heightAnimator.addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        menuLayout.visibility = View.GONE
                                    }
                                })
                            }
                        }
                        handler.postDelayed(this, 1000)
                    }
                }, 1000)


                floatingIcon.setOnClickListener {
                    if (!isHalfHideEnabled) {

                        if (menuLayout.visibility == View.GONE) {
                            menuLayout.layoutParams.width = floatingIcon.width

                            menuLayout.post {
                                menuLayout.measure(
                                    View.MeasureSpec.UNSPECIFIED,
                                    View.MeasureSpec.UNSPECIFIED
                                )
                                val targetHeight = menuLayout.measuredHeight
                                Log.d("FloatingWindow", "菜单内容高度: $targetHeight")

                                val params = menuLayout.layoutParams as RelativeLayout.LayoutParams
                                params.topMargin =
                                    (floatingIcon.height * 0.001f).toInt()
                                menuLayout.layoutParams = params

                                menuLayout.visibility = View.VISIBLE

                                ObjectAnimator.ofInt(
                                    menuLayout, "layoutParams.height",
                                    0, targetHeight
                                ).apply {
                                    duration = animationDuration
                                    addUpdateListener { animation ->
                                        val layoutParams = menuLayout.layoutParams
                                        layoutParams.height = (animation.animatedValue as Int)
                                        menuLayout.layoutParams = layoutParams
                                    }
                                    start()
                                    Log.d("FloatingWindow", "展开动画启动")
                                }

                                ObjectAnimator.ofFloat(
                                    menuLayout, "alpha", 0f, 1f
                                ).apply {
                                    duration = animationDuration
                                    start()
                                    Log.d("FloatingWindow", "透明度动画启动")
                                }
                            }
                        } else {
                            val heightAnimator = ObjectAnimator.ofInt(
                                menuLayout, "layoutParams.height",
                                menuLayout.height, 0
                            ).apply {
                                duration = animationDuration
                                start()
                            }

                            ObjectAnimator.ofFloat(
                                menuLayout, "alpha", 1f, 0f
                            ).apply {
                                duration = animationDuration
                                start()
                            }

                            heightAnimator.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    menuLayout.visibility = View.GONE
                                }
                            })
                        }
                    }

                    lastMoveTime = System.currentTimeMillis()
                    if (isHalfHideEnabled) {
                        FloatingX.configControl().setEnableHalfHide(false)
                        isHalfHideEnabled = false
                    }

                }


                runAndStopButton!!.setOnClickListener {
                    if (isStarted) {
                        Log.d("FloatingWindow", "停止按钮被点击")
                        runAndStopButton!!.setImageResource(R.drawable.run)
                        isStarted = false
                        CoreEnv.globalVM.get<Script>()?.exit()
                    } else {
                        Log.d("FloatingWindow", "开始按钮被点击")
                        runAndStopButton!!.setImageResource(R.drawable.stop) // 切换图标到停止
                        isStarted = true

                        if (CoreEnv.executorMap.cancelAndClean("run") ) {

                            val executor = DebugController.createNamedThreadPool()

                            CoreEnv.executorMap["run"] = executor.submit {

                                XLog.i("运行命令")

                                CoreEnv.globalVM.get<Script>()?.run()

                                runAndStopButton!!.setImageResource(R.drawable.run)
                                isStarted = false

                            }

                        }




                    }
                }

                logcatButton?.apply {
                    setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    var isStartedDebug = false
                    setOnClickListener {
                        if (isStartedDebug) {
                            XLog.d( "关闭按钮被点击")
                            setImageResource(R.drawable.logcat_icon) // 切换图标到开始
                            setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                            logcat?.hide()
                            isStartedDebug = false
                        } else {
                            XLog.d( "展开按钮被点击")
                            setImageResource(R.drawable.logcat_icon)
                            setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)

                            logcat = Logcat(baseEnv, this@Console).also {
                                globalVM.add(it)
                                it.show()
                            }
                            isStartedDebug = true
                        }
                    }
                }

                x.setOnClickListener {
                    FloatingX.controlOrNull()?.hide()
                }


            }
        }

    }

    fun hide() {
        runOnUi {
            FloatingX.controlOrNull()?.hide()
        }

    }

    fun cancel() {
        runOnUi {
            FloatingX.controlOrNull()?.cancel()
        }

    }

}