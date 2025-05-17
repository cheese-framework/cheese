package net.codeocean.cheese.backend.impl

import android.content.Intent
import com.elvishew.xlog.XLog
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import net.codeocean.cheese.core.CoreApp.Companion.globalVM
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.Hot
import net.codeocean.cheese.core.IAction
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.api.WebView
import kotlin.coroutines.resume

object WebViewImpl:WebView {
    var iWebView: IAction? =null

    override fun inject(iWebView: IAction) {
        this.iWebView=iWebView
    }

    override fun runWebView(id: String) {
        Hot.checkUpdate(2) {
            val intent =  Intent(CoreEnv.envContext.context, Class.forName("net.codeocean.cheese.core.activity.StubWebActivity$$id")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            CoreEnv.envContext.context.startActivity(intent)
        }
    }

    override fun document(methodName: String, vararg args: Any?): Any? {
        return try {
            runBlocking {
                try {
                    val jsArgs = args.filterNotNull().joinToString(separator = ", ") { arg ->
                        when (arg) {
                            is String -> "\"${arg.replace("\"", "\\\"")}\"" // 处理字符串参数
                            is Int, is Double, is Boolean -> arg.toString() // 处理其他基本数据类型
                            else -> "\"${arg.toString().replace("\"", "\\\"")}\"" // 默认处理为字符串
                        }
                    }
                    var jsCode: String? = null
                    if (methodName.contains("()")) {
                        val newStr = methodName.replace(Regex("\\(.*?\\)"), "")
                        jsCode = if (jsArgs.isEmpty()) {
                            "javascript:document.$newStr();"
                        } else {
                            "javascript:document.$newStr($jsArgs);"
                        }
                    } else {
                        jsCode = if (jsArgs.isEmpty()) {
                            "javascript:document.$methodName"
                        } else {
                            "javascript:document.$methodName = $jsArgs;"
                        }
                    }

                    try {

                        withTimeout(10000L) {  // 设置超时时间为 10 秒
                            eval(jsCode)
                        }
                    } catch (e: TimeoutCancellationException) {
                        println("超时：${e.message}")
                        null
                    }
                } catch (e: TimeoutCancellationException) {
                    println("失败：${e.message}")
                    null
                }
            }

        } catch (e: Throwable) {
            println("失败：${e.message}")
            false
        }

    }

    override fun window(methodName: String, vararg args: Any?): Any? {
        return try {
            runBlocking {
                try {
                    val jsArgs = args.filterNotNull().joinToString(separator = ", ") { arg ->
                        when (arg) {
                            is String -> "\"${arg.replace("\"", "\\\"")}\"" // 处理字符串参数
                            is Int, is Double, is Boolean -> arg.toString() // 处理其他基本数据类型
                            else -> "\"${arg.toString().replace("\"", "\\\"")}\"" // 默认处理为字符串
                        }
                    }
                    var jsCode: String? = null
                    if (methodName.contains("()")) {
                        val newStr = methodName.replace(Regex("\\(.*?\\)"), "")
                        jsCode = if (jsArgs.isEmpty()) {
                            "javascript:window.$newStr();"
                        } else {
                            "javascript:window.$newStr($jsArgs);"
                        }
                    } else {
                        jsCode = if (jsArgs.isEmpty()) {
                            "javascript:window.$methodName"
                        } else {
                            "javascript:window.$methodName = $jsArgs;"
                        }
                    }
                    try {
                        withTimeout(5000L) {  // 设置超时时间为 10 秒
                            eval(jsCode)
                        }
                    } catch (e: TimeoutCancellationException) {
                        XLog.e("超时：${e.message}")
                        null
                    }
                } catch (e: TimeoutCancellationException) {
                    XLog.e("失败：${e.message}")
                    null
                }
            }

        } catch (e: Throwable) {
            XLog.e("失败：${e.message}")
            false
        }

    }


    private suspend fun eval(jsCode: String): String =
        suspendCancellableCoroutine { cont ->
            runOnUi {
                try {
                    globalVM.get<android.webkit.WebView>()?.evaluateJavascript(jsCode) { jsResult ->
                        if (!cont.isActive) return@evaluateJavascript
                        cont.resume(parseJson(jsResult).toString())
                    }
                } catch (e: UninitializedPropertyAccessException) {
                    throw Error(e)
                }
            }
        }

    private fun parseJson(jsonString: String?): Any? {
        if (jsonString.isNullOrEmpty()) return null

        return try {
            val jsonElement: JsonElement = JsonParser.parseString(jsonString)
            when {
                jsonElement.isJsonPrimitive -> {
                    val jsonPrimitive = jsonElement.asJsonPrimitive
                    when {
                        jsonPrimitive.isBoolean -> jsonPrimitive.asBoolean
                        jsonPrimitive.isNumber -> jsonPrimitive.asNumber
                        else -> jsonPrimitive.asString
                    }
                }

                jsonElement.isJsonObject -> {
                    jsonElement.asJsonObject
                }

                jsonElement.isJsonArray -> {
                    jsonElement.asJsonArray.map { parseJson(it.toString()) }
                }

                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}