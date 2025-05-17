package net.codeocean.cheese.core

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.elvishew.xlog.XLog
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.core.CoreApp.Companion
import net.codeocean.cheese.core.api.Path
import net.codeocean.cheese.core.utils.AssetsUtils
import net.codeocean.cheese.core.utils.FilesUtils
import net.codeocean.cheese.backend.impl.PathImpl.SD_ROOT_DIRECTORY
import net.codeocean.cheese.core.utils.ZipUtils
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

object CoreEnv {
    val Tag = this.javaClass.name
    var runTime: RuntimeConfig = RuntimeConfig()
    val executorMap: MutableMap<String, Future<*>> = mutableMapOf()
    var sdk_version = "1.0.12"
    var recordScreen: RecordScreenNest = RecordScreenNest()
    var globalVM = CoreApp.globalVM
    fun isFirstLaunch(): Boolean {
        val prefs = envContext.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_first_launch", true).also { isFirst ->
            if (isFirst) {
                prefs.edit().putBoolean("is_first_launch", false).apply()
            }
        }
    }

    var error = Error("", "")

    fun isNewerVersion(current: String?, latest: String?): Boolean {
        if (latest.isNullOrBlank()) return false // 远程无版本，不是新版
        if (current.isNullOrBlank()) return true  // 本地无版本，视为需要更新

        val currentParts = current.split(".")
        val latestParts = latest.split(".")

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val cur = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            val lat = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (lat > cur) return true
            if (lat < cur) return false
        }
        return false // 完全相等
    }


    fun isUpdateSdk(context: Context): Boolean {
        val configFile = File(
            PathImpl.SD_ROOT_DIRECTORY,
            "sdk/config.json"
        ).absolutePath
        if (!FilesUtils.isFile(configFile)) {
            XLog.tag(Tag).i("文件不存在")
            return true
        }
        val latest = AssetsUtils.readJson(context, "sdkconfig.json", "version").toString()
        val current = FilesUtils.readJson(configFile, "version").toString()
        return isNewerVersion(current, latest)
    }


    fun Context.performFirstTimeSetup() {
        if (AssetsUtils.isFile(this, "sdk.zip")) {
            if (isUpdateSdk(this)) {
                val zipFile =File(PathImpl.SD_ROOT_DIRECTORY,"sdk.zip")
                if (AssetsUtils.copyFileToSD(
                        this,
                        "sdk.zip",
                        zipFile.absolutePath
                    )
                ) {

                    ZipUtils.decompress(
                        zipFile.path,
                        PathImpl.SD_ROOT_DIRECTORY.path,
                        ""
                    )

                    XLog.tag(Tag).i("更新Sdk成功")
                    File(PathImpl.SD_ROOT_DIRECTORY, "sdk/components/project/demo").copyRecursively(
                        PathImpl.SD_DEMO_DIRECTORY,
                        true
                    )
                } else {
                    XLog.tag(Tag).e("更新Sdk失败")
                }
            } else {
                XLog.tag(Tag).e("Sdk为最新版")
            }
        }
    }

    val envContext: EnvContext
        get() = EnvContext(
            context = CoreApp.globalVM.get<CoreApp>()!!,
            activity = GlobalActivity.getCurrentActivity()
        )


    data class EnvContext(
        var context: Context,
        var activity: Activity?
    )

    data class Error(
        var msg: String,
        var stack: String?
    )

    data class RecordScreenNest(
        var mediaProjectionManager: MediaProjectionManager? = null,
        var mediaProjection: MediaProjection? = null,
        var virtualDisplay: VirtualDisplay? = null,
        var imageReader: ImageReader? = null,
        var bitmap: Bitmap? = null,
    )


    data class RuntimeConfig(
        var ip: String = "127.0.0.1:8080",
        var connect: Boolean = false,
        var state: Boolean = false,
        var binding: BindingType = BindingType.JAVASCRIPT,
        var isThrowException: Boolean = false,
        // 运行模式配置
        var isDebugMode: Boolean = true,     // 调试模式（true=调试，false=正式）
        var isRemoteMode: Boolean = false    // 远程模式（true=远程，false=本地）
    )


    enum class BindingType {
        JAVASCRIPT
    }


}


/**
 * 全局状态容器，支持多类型存储和生命周期感知的监听
 */
class GlobalViewModel : ViewModel() {

    // 存储所有类型的值
    private val values = ConcurrentHashMap<Class<*>, Any>()

    // 存储所有监听器，弱引用防止内存泄漏
    private val listeners = ConcurrentHashMap<Class<*>, MutableSet<WeakReference<(Any) -> Unit>>>()

    /**
     * 原子更新指定类型的值，通知所有监听器
     */
    fun <T : Any> add(clazz: Class<T>, value: T) {
        values.compute(clazz) { _, oldValue ->
            if (oldValue != value) {
                listeners[clazz]?.removeAll { ref ->
                    ref.get()?.let {
                        it(value)
                        false
                    } ?: true // 引用失效移除
                }
                value
            } else {
                oldValue
            }
        }
    }

    /**
     * 泛型安全更新方法
     */
    inline fun <reified T : Any> add(value: T) {
        println("添加目标：" + T::class.java.name)
        add(T::class.java, value)
    }

    /**
     * 获取指定类型的值
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: Class<T>): T? {
        return values[clazz] as? T
//        return values[clazz] as? T
//            ?: throw IllegalStateException("请先调用 update() 初始化 ${clazz.simpleName}")
    }


    /**
     * 泛型安全获取方法
     */
    inline fun <reified T : Any> get(): T? {
        return get(T::class.java)
    }

    /**
     * 添加弱引用监听器
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> addListener(clazz: Class<T>, listener: (T) -> Unit) {
        val wrapper: (Any) -> Unit = { value -> listener(value as T) }

        // 立即触发当前值
        values[clazz]?.let {
            listener(it as T)
        }

        // 添加弱引用监听器
        listeners.computeIfAbsent(clazz) { ConcurrentHashMap.newKeySet() }
            .add(WeakReference(wrapper))
    }

    /**
     * 泛型安全添加监听器
     */
    inline fun <reified T : Any> addListener(noinline listener: (T) -> Unit) {
        addListener(T::class.java, listener)
    }

    /**
     * 移除指定监听器（基于引用相等性）
     */
    fun <T : Any> removeListener(clazz: Class<T>, listener: (T) -> Unit) {
        listeners[clazz]?.removeIf { ref ->
            ref.get()?.let {
                val refListener = it as? (T) -> Unit
                refListener === listener
            } ?: true
        }
    }

    /**
     * 泛型安全移除监听器
     */
    inline fun <reified T : Any> removeListener(noinline listener: (T) -> Unit) {
        removeListener(T::class.java, listener)
    }

    /**
     * ViewModel 清理，释放所有资源
     */
    override fun onCleared() {
        values.clear()
        listeners.clear()
    }

    companion object {
        private val viewModelStore = ViewModelStore()

        /**
         * 获取全局单例实例（线程安全懒加载）
         */
        val globalVM: GlobalViewModel by lazy {
            ViewModelProvider(viewModelStore, Factory)[GlobalViewModel::class.java]
        }

        private object Factory : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GlobalViewModel() as T
            }
        }
    }
}


