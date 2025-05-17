package net.codeocean.cheese.frontend.javascript

import com.elvishew.xlog.XLog
import net.codeocean.cheese.backend.impl.PluginsImpl.Companion.mDexClassLoaders
import org.mozilla.javascript.DefiningClassLoader
import java.lang.ClassLoader

class JSClassLoader(
    parent: ClassLoader,
) : DefiningClassLoader(parent) {
    override fun findClass(name: String?): Class<*> {
        return super.findClass(name)
    }


    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        var findLoadedClass = findLoadedClass(name)
        if (findLoadedClass != null) return findLoadedClass
        for (loader in mDexClassLoaders.values) {
            try {
                findLoadedClass = loader.loadClass(name)
                if (findLoadedClass != null) return  findLoadedClass
            } catch (e: Exception) {
                XLog.e(JSClassLoader::class.java.name+"报错："+e.message)
            }
        }


        return super.loadClass(name, resolve)
    }

    override fun findLibrary(name: String): String? {
        return super.findLibrary(name)
    }

}