package net.codeocean.cheese.core

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.elvishew.xlog.XLog
import com.hjq.toast.Toaster
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.codeocean.cheese.core.runtime.ScriptExecutionController

import org.tomlj.Toml
import org.tomlj.TomlParseResult
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.URL
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.exitProcess
abstract class Action {
    abstract suspend fun run(vararg parameters: Any): Any?

    companion object {
        fun runAction(action: Action, vararg parameters: Any): Any? {
            return runBlocking {
                return@runBlocking action.run(*parameters)
            }
        }
    }

}
object Misc {
    val TAG: String = this.javaClass.simpleName



    fun <T> release(resource: T): Boolean {
        try {

            when (resource) {
                is InputStream -> resource.close()
                is Bitmap -> resource.recycle()
                else -> return false
            }
            return true
        } catch (e: Exception) {
            XLog.e("Failed to release resource: ${e.message}")
        }
        return false
    }

    fun runOnUi(block: () -> Unit) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            block()
        } else {
            Handler(Looper.getMainLooper()).post(block)
        }
    }
    fun isPortInUse(port: Int): Boolean {
        var serverSocket: ServerSocket? = null
        return try {
            serverSocket = ServerSocket(port)
            serverSocket.reuseAddress = true
            false // 端口没有被占用
        } catch (e: IOException) {
            true // 端口被占用
        } finally {
            serverSocket?.close()
        }
    }
    var xy = false
    fun showTermsAndConditionsDialog() {
        val termsAndConditions = """
请您先点击阅读协议按钮详细阅读cheese系列产品和服务-用户协议。
只有在您完全接受并遵守本用户协议的情况下，您才拥有使用本产品及相关服务的资格。
若无法访问，请直接联系我们(邮箱：3560000009@qq.com)以获取最新协议内容。
开始使用本产品和相关服务即表示您已阅读并同意全部协议条款。

点击“同意”按钮表示您已阅读并同意全部协议条款，点击“不同意”按钮将退出本软件的使用。  
        """.trim()
        // 这里只是一个示例字符串，实际中您应该使用更长的协议文本
        val sh = CoreEnv.envContext.activity?.let {
            AlertDialog.Builder(it)
                .setTitle("《cheese系列产品-用户许可使用协议》")
                .setMessage(termsAndConditions)
                .setPositiveButton("同意") { _, _ ->
                    if (xy) {

                        Toaster.show("欢迎使用！")
                        CoreFactory.getPersistentStore().save("agreement", "state", true)
                    } else {

                        Toaster.show("请先阅读协议！")
                        showTermsAndConditionsDialog()
                    }
                }
                .setNegativeButton("不同意") { _, _ ->
                    xy = false
                    exitProcess(0)
                }
                .setNeutralButton("阅读协议") { _, _ ->
                    thread {
                        Thread.sleep(500)
                        it.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://cheese.codeocean.net/agreement.html")
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        Thread.sleep(500)
                        runOnUi {
                            showTermsAndConditionsDialog()
                        }
                        Thread.sleep(1000)
                        xy = true

                    }

                }
        }
        val ok = CoreFactory.getPersistentStore().get("agreement", "state") as? Boolean ?: false
        if (!ok) {
            sh?.show()
        }


    }
    fun isForegroundServiceRunning(id: Int): Boolean {
        val notificationManager =
            CoreEnv.envContext.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications
        for (notification in activeNotifications) {
            if (notification.id == id) {
                return true
            }
        }
        return false
    }
    fun isServiceRunning(context: Context, serviceClass: Class<out Service>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)

        for (service in runningServices) {
            if (service.service == ComponentName(context, serviceClass)) {
                return true
            }
        }
        return false
    }
    fun extractPackageName(apkFilePath: String): String? {
        val pm: PackageManager = CoreEnv.envContext.context.packageManager
        val packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0)
        return packageInfo?.packageName
    }
    fun launchWithExpHandler(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = GlobalScope.launch(context + ExceptionHandler, start, block)
    val ExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            XLog.e("执行失败： ${throwable.message ?: "$throwable"}")
            throwable.printStackTrace()
        }
    }
    val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun getCPUABI(): String {
        return  Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
    }


    fun checkUrl(urlStr: String?): Boolean {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection()
            conn.connectTimeout = 5000
            conn.connect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun parseToml(tomlPath: String): TomlParseResult {
        return FileInputStream(File(tomlPath)).use { fis ->
            val content = fis.bufferedReader().use { it.readText() }
            val result = Toml.parse(content)
            result.errors().forEach { error -> System.err.println(error) }
            result
        }
    }

    fun extractIpAddress(url: String): String? {
        val regex = Regex("^(?:http://|https://)?([^:/]+)")
        val matchResult = regex.find(url)
        return matchResult?.groups?.get(1)?.value
    }


}