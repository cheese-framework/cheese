package net.codeocean.cheese.backend.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import dalvik.system.DexClassLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.CoreFactory
import net.codeocean.cheese.core.Misc.extractPackageName
import net.codeocean.cheese.core.api.Plugins
import java.io.File
import java.io.FileOutputStream

class PluginsImpl() : Plugins, BaseEnv {
    private val mCacheDir = File("")
    private  lateinit var resources: Resources
    var pkg:String =""
    override fun install(path: String): Boolean = runBlocking {
        pkg = extractPackageName(path).toString()
        var pluginsPath:File
        if (!getCachePath(pkg).exists()) {
            makeAppPath(pkg, path)
            extractLibsFromApk(path, getAppLibPath(pkg).toString())
            pluginsPath =File(getCachePath(pkg),"base.apk")
            File (path).copyTo(pluginsPath)
            pluginsPath=getCachePath(pkg)
        }else{
            pluginsPath =File(path)
        }

        val deferredRes = async { loadRes(pluginsPath.absolutePath) }
        val deferredDex = async {
            loadDex(
                pluginsPath.absolutePath,
                File(File(getAppLibPath(pkg).toString()).absolutePath + File.separator + Build.CPU_ABI).absolutePath
            ).also {
                mDexClassLoaders[pkg] = it
            }
        }
        // 等待异步任务完成
        deferredRes.await()
        deferredDex.await()
        return@runBlocking getCachePath(pkg).exists()
    }

    override fun createContext(): Context = object : ContextWrapper(cx) {
        override fun getAssets(): AssetManager = resources.assets
    }

    override fun uninstall(): Boolean {
        val pkgPath = getCachePath(pkg)
        if (pkgPath.exists()) {
            mDexClassLoaders.remove(pkg)
            return pkgPath.deleteRecursively()
        }
        return true
    }

    override fun getClassLoader(): ClassLoader? = mDexClassLoaders[pkg]

    private fun loadDex(f: String, librarySearchPath: String): DexClassLoader {
        val file = File(f)
        require(file.exists()) { "File not found: ${file.path}" }
        return DexClassLoader(
            file.path,
            mCacheDir.path,
            librarySearchPath,
            cx.classLoader,
        )
    }

    @SuppressLint("PrivateApi")
    private fun loadRes(apkFilePath: String) {
        try {
            val assetManager = AssetManager::class.java.newInstance()
            AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java).invoke(
                assetManager, apkFilePath
            )
            resources = Resources(
                assetManager,
                cx.resources.displayMetrics,
                cx.resources.configuration
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAppLibPath(pkg: String): File {
        val path = File(
            APP_DIRECTORY,
            File(File(CoreFactory.getPersistentStore().get("app", pkg) as String), "lib").path
        ).path
        net.codeocean.cheese.core.utils.FilesUtils.create(path)
        return File(path)
    }

    private fun makeAppPath(pkg: String, path: String): File {
        val sha256 = CoreFactory.getAPP().getApkSha256(path)
        val appPath = (CoreFactory.getPersistentStore().get("app", pkg))
        if (appPath == null) {
            println("创建App缓存路径")
            CoreFactory.getPersistentStore().save("app", pkg, "${pkg}_${sha256}")
            return File(APP_DIRECTORY, appPath.toString())
        }
        return File(APP_DIRECTORY, "${pkg}_${sha256}")
    }

    private fun extractLibsFromApk(apkFilePath: String, destinationFolderPath: String) {
        val apkFile = java.util.zip.ZipFile(apkFilePath)
        val entries = apkFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.isDirectory || !entry.name.startsWith("lib/")) {
                continue
            }
            val entryName = entry.name
            val libName = entryName.substring(entryName.indexOf('/') + 1)
            val libFile = File(destinationFolderPath + File.separator + libName)
            if (libFile.getParentFile() != null && !libFile.getParentFile()!!.exists()) {
                libFile.getParentFile()?.mkdirs()
            }
            val inputStream = apkFile.getInputStream(entry)
            val outputStream = FileOutputStream(libFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            inputStream.close()
            outputStream.close()
        }
        apkFile.close()
    }
    companion object:BaseEnv {
        private val DATA_DIRECTORY: File = File(cx.cacheDir.parentFile, "data")
        private val APP_DIRECTORY: File = File(DATA_DIRECTORY, "app")
        val mDexClassLoaders: MutableMap<String, DexClassLoader> = HashMap()
        fun getCachePath(pkg: String): File {
            return File(APP_DIRECTORY, CoreFactory.getPersistentStore().get("app", pkg).toString())
        }
    }

}