package net.codeocean.cheese.core.api

import android.content.Context
import com.hjq.window.EasyWindow
import net.codeocean.cheese.backend.impl.PluginsImpl
import net.codeocean.cheese.backend.impl.PluginsImpl.Companion.getCachePath
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.utils.PermissionsUtils
import net.codeocean.cheese.core.utils.PermissionsUtils.FLOATING
import java.io.File

interface Plugins {
    fun install(path:String):Boolean
    fun uninstall():Boolean
    fun getClassLoader(): ClassLoader?
    fun createContext():Context

    companion object {
        fun hasPlugins(pkg: String): Plugins? {
            val pluginsPath = File(getCachePath(pkg),"base.apk")
            if (pluginsPath.exists()){
                PluginsImpl().install(pluginsPath.absolutePath)
                return PluginsImpl()
            }
            return null
        }
    }

}