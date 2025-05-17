package net.codeocean.cheese.backend.impl

import android.app.PendingIntent
import android.content.*
import android.os.*
import androidx.core.content.ContextCompat
import com.elvishew.xlog.XLog
import com.termux.shared.termux.TermuxConstants

import java.util.concurrent.atomic.AtomicInteger

class TermuxComm(private val context: Context) {
    // 使用设备ID后缀避免多设备请求码冲突
    private val requestCodeCounter = AtomicInteger((Build.ID.hashCode() and 0xFFFF))
    private var resultReceiver: BroadcastReceiver? = null
    private var currentAction: String? = null

    /**
     * 执行Termux命令
     * @param command 要执行的命令
     * @param background 是否后台运行
     * @param timeoutMs 超时时间（毫秒）
     * @param callback 结果回调
     */
    fun execute(
        command: String,
        background: Boolean = false,
        timeoutMs: Long = 9999999999999999,
        callback: (stdout: String, stderr: String, exitCode: Int) -> Unit
    ) {
        val requestCode = requestCodeCounter.incrementAndGet()
        val resultAction =
            "${context.packageName}.TERMUX_RESULT_${requestCode}_${System.currentTimeMillis()}"


        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            unregisterReceiver()
            callback("", "Command timeout after ${timeoutMs}ms", -2)
        }


        registerResultReceiver(resultAction) { stdout, stderr, exitCode ->
            timeoutHandler.removeCallbacks(timeoutRunnable)
            callback(stdout, stderr, exitCode)
        }


        timeoutHandler.postDelayed(timeoutRunnable, timeoutMs)


        try {
            val execIntent = createCommandIntent(command, background, resultAction, requestCode)
            ContextCompat.startForegroundService(context, execIntent)
        } catch (e: Exception) {
            timeoutHandler.removeCallbacks(timeoutRunnable)
            handleExecutionError(e, callback)
        }
    }

    private fun registerResultReceiver(
        action: String,
        callback: (String, String, Int) -> Unit
    ) {
        unregisterReceiver()

        resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == action) {
                    unregisterReceiver()
                    handleCommandResult(intent, callback)
                }
            }
        }.also {
            context.registerReceiver(it, IntentFilter(action).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
            })
            currentAction = action
        }
    }

    private fun createCommandIntent(
        command: String,
        background: Boolean,
        resultAction: String,
        requestCode: Int
    ): Intent = Intent().apply {

        setClassName(
            TermuxConstants.TERMUX_PACKAGE_NAME,
            TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME
        )

        action = TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND
        putExtra(
            TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH,
            "\$PREFIX/bin/bash"
        )
        putExtra(
            TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS,
            arrayOf("-c", command)
        )
        putExtra(
            TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_WORKDIR,
            TermuxConstants.TERMUX_HOME_DIR_PATH
        )
        putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, background)

        putExtra(
            TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_PENDING_INTENT,
            createPendingIntent(resultAction, requestCode)
        )
    }

    private fun createPendingIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(action).apply {
                `package` = context.packageName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.FLAG_MUTABLE else 0
        )
    }

    private fun handleCommandResult(intent: Intent, callback: (String, String, Int) -> Unit) {
        val bundle =
            intent.getBundleExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE)
                ?: return callback("", "No result bundle", -1)

        try {
            callback(
                bundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT)
                    .orEmpty(),
                bundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR)
                    .orEmpty(),
                bundle.getInt(
                    TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE,
                    -1
                )
            )
        } catch (e: Exception) {
            callback("", "Result parsing error: ${e.message}", -3)
        }
    }

    private fun handleExecutionError(e: Exception, callback: (String, String, Int) -> Unit) {
        val errorMsg = when (e) {
            is SecurityException -> "Missing RUN_COMMAND permission. Check Termux settings."
            is TransactionTooLargeException -> "Command too large to execute"
            else -> "Execution failed: ${e.message}"
        }
        callback("", errorMsg, -1)
    }

    private fun unregisterReceiver() {
        resultReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                XLog.e(e)
            }
            resultReceiver = null
            currentAction = null
        }
    }

    fun cleanup() {
        unregisterReceiver()
    }
}