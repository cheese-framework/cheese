package net.codeocean.cheese


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.elvishew.xlog.XLog
import com.tencent.paddleocrncnn.PaddleOCRNcnn
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import net.codeocean.cheese.backend.impl.TermuxComm
import net.codeocean.cheese.bottomnav.BottomNav
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.CoreEnv.performFirstTimeSetup
import net.codeocean.cheese.core.Misc.showTermsAndConditionsDialog
import net.codeocean.cheese.core.utils.PermissionsUtils
import net.codeocean.cheese.core.utils.PermissionsUtils.initPermissions

import net.codeocean.cheese.ui.theme.CheeseTheme
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class ProfileViewModel : ViewModel() {
    val appLogger = mutableStateListOf<String>()
    val connectData = mutableStateOf(false)
}

val appLogger = ProfileViewModel().appLogger
val connectData = ProfileViewModel().connectData

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CheeseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BottomNav()
                }
            }
        }

        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                applicationContext.assets
            )
        )
        showTermsAndConditionsDialog()
        captureLogcatToFile(this)
    }

    private fun captureLogcatToFile(context: Context) {
        try {
            // 清空旧日志（可选）
            Runtime.getRuntime().exec("logcat -c").waitFor()

            // 获取logcat进程
            val process = Runtime.getRuntime().exec("logcat -v threadtime")

            // 获取存储路径
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (documentsDir == null) {
                Log.e("LogcatCapture", "Documents directory is not available.")
                return
            }

            // 创建日志文件
            val logFile = File(documentsDir, "writelog_${System.currentTimeMillis()}.txt")

            // 确保目录存在
            if (!documentsDir.exists()) {
                documentsDir.mkdirs() // 创建所有必要的父目录
            }

            // 创建文件（如果不存在）
            if (!logFile.exists()) {
                logFile.createNewFile()
            }

            // 捕获日志
            thread {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    FileWriter(logFile, true).use { writer -> // true for appending logs
                        val logLines = mutableListOf<String>()
                        var line: String?
                        var isStackTrace: Boolean
                        while (reader.readLine().also { line = it } != null) {
                            // 如果是堆栈，加入到当前堆栈信息
                            isStackTrace =
                                line?.contains("Exception") == true || line?.contains("at ") == true
                            if (isStackTrace) {
                                logLines.add(line!!)
                            } else {
                                // 不是堆栈的下一行，先保存之前的堆栈信息
                                if (logLines.isNotEmpty()) {
                                    // 合并堆栈信息并写入文件
                                    val fullLog = logLines.joinToString("\n")
                                    writer.write("$fullLog\n")
                                    writer.flush() // 确保写入文件
                                    // 添加到内存日志（可选，建议限制数量）
                                    if (appLogger.size > 1000) { // 防止内存溢出
                                        appLogger.clear()
                                    }
                                    appLogger.add(fullLog)
                                    logLines.clear()
                                } else {
                                    writer.write("$line\n")
                                    writer.flush() // 确保写入文件
                                    if (appLogger.size > 1000) { // 防止内存溢出
                                        appLogger.clear()
                                    }
                                    appLogger.add(line!!)
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            XLog.e("LogcatCapture", "Error: ${e.message}", e)
        }
    }


}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CheeseTheme {
        Greeting("Android")
    }
}