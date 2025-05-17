package net.codeocean.cheese.frontend.javascript

import android.annotation.SuppressLint
import android.os.Looper
import com.elvishew.xlog.XLog
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.core.CoreFactory
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.Hot
import net.codeocean.cheese.core.Script
import net.codeocean.cheese.core.api.Path
import net.codeocean.cheese.core.exception.ScriptInterruptedException
import net.codeocean.cheese.core.runtime.ScriptExecutionController
import net.codeocean.cheese.core.utils.ScriptLogger
import org.mozilla.javascript.*
import org.mozilla.javascript.commonjs.module.ModuleScope
import org.mozilla.javascript.tools.shell.Global
import java.io.File

class JavaScript(
    private val androidContext: android.content.Context,
    private val path: Path = PathImpl
) : Script {

    private val jsMainScriptPath: String
        get() = path.JS_DIRECTORY.resolve("main.js").path

    init {
        ensureContextFactory()
        CoreEnv.globalVM.add<Script>(this)
    }


    override fun run(workingDir: File) {

        Hot.checkUpdate(1) {
            CoreEnv.runTime.state = true
            val config = ScriptExecutionController.parseCheeseToml(workingDir) ?: run {
                ScriptLogger.e("配置文件解析失败")
                CoreEnv.runTime.state = false
                return@checkUpdate
            }

            val mainScriptName = config.getString("main") ?: run {
                ScriptLogger.e("配置文件缺少 'main' 字段")
                CoreEnv.runTime.state = false
                return@checkUpdate
            }

            val jsPath = File(File(workingDir, "main/js"), "$mainScriptName.js")
            if (!jsPath.exists()) {
                ScriptLogger.e("找不到脚本: ${jsPath.path}")
                CoreEnv.runTime.state = false
                return@checkUpdate
            }

            val content = runCatching {
                jsPath.readText()
            }.onFailure { e ->
                ScriptLogger.e("读取脚本失败: ${e.message}", e)
                CoreEnv.runTime.state = false
                return@checkUpdate
            }.getOrNull() ?: run {
                ScriptLogger.e("读取脚本内容为空")
                CoreEnv.runTime.state = false
                return@checkUpdate
            }

            execute(content)
            CoreEnv.runTime.state = false
        }


    }

    override fun exit() {
        CoreEnv.runTime.isThrowException = true
        CoreEnv.executorMap.values.forEach { it.cancel(true) }

        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw ScriptInterruptedException()
        }
    }

    override fun convertNativeObjectToMap(nativeObj: Any): HashMap<Any, Any> {
        val map = HashMap<Any, Any>()
        if (nativeObj is NativeObject) {
            val ids = nativeObj.ids
            for (id in ids) {
                val key = id.toString()
                val value = nativeObj.get(key, nativeObj)
                map[key] = when (value) {
                    is NativeObject -> convertNativeObjectToMap(value)
                    else -> value
                }
            }
        }
        return map
    }

    override fun convertNativeArrayToList(value: Any): ArrayList<Any> {
        val arrayList = ArrayList<Any>().apply {
            if (value is NativeArray) {
                // 获取 NativeArray 的长度
                val length = value.getLength().toInt()
                // 遍历并添加每个元素
                for (i in 0 until length) {
                    add(value.get(i, null))
                }
            }
        }

        return arrayList
    }

    private fun execute(jsCode: String) {
        val context = Context.enter().apply {
            optimizationLevel = -1
            languageVersion = Context.VERSION_ES6
        }

        try {
            val globalScope = Global(context).apply {
                installRequire(
                    context,
                    listOf(
                        "${path.WORKING_DIRECTORY.path}/node_modules",
                        path.JS_DIRECTORY.path
                    ),
                    false
                )
            }
            ScriptableObject.putProperty(globalScope, "error", CoreEnv.error)
            val moduleScope = ModuleScope(
                globalScope,
                path.JS_DIRECTORY.toURI(),
                path.JS_DIRECTORY.toURI()
            ).apply {
                ImporterTopLevel.init(context, this, true)
            }

            logTaskStart()
            evaluateInitScript(context, moduleScope)

            context.evaluateString(moduleScope, jsCode, jsMainScriptPath, 1, null)


        } catch (e: RhinoException) {
            ScriptLogger.e("JS 执行异常: ${e.message}", e)
        } catch (e: Exception) {
            ScriptLogger.e("未知异常: ${e.message}", e)
        } finally {
            Context.exit()  // 确保退出上下文
            logTaskEnd()
        }
    }

    private fun ensureContextFactory() {
        if (!ContextFactory.hasExplicitGlobal()) {
            ContextFactory.initGlobal(object : ContextFactory() {
                override fun createClassLoader(parent: ClassLoader?) =
                    JSClassLoader(androidContext.classLoader)

                override fun onContextCreated(cx: Context) {
                    val factory = cx.factory
                    if (factory.applicationClassLoader == null) {
                        factory.initApplicationClassLoader(JSClassLoader(androidContext.classLoader))
                    }
                    super.onContextCreated(cx)
                }
            })
        }
    }

    private fun logTaskStart() {
        val scriptName =
            if (!CoreEnv.runTime.isDebugMode) "main.js" else File(jsMainScriptPath).name
        ScriptLogger.i("***Task ($scriptName) 运行开始***")
    }

    private fun logTaskEnd() {
        val scriptName =
            if (!CoreEnv.runTime.isDebugMode) "main.js" else File(jsMainScriptPath).name
        ScriptLogger.i("***Task ($scriptName) 运行结束***")
    }

    private fun evaluateInitScript(context: Context, scope: Scriptable) {
        runCatching {
            androidContext.assets.open("init.js").use {
                it.readBytes().toString(Charsets.UTF_8)
            }
        }.onSuccess { initScript ->
            context.evaluateString(scope, initScript, "init.js", 1, null)
        }.onFailure {
            ScriptLogger.w("初始化脚本加载失败: ${it.message}")
        }
    }
}
