package net.codeocean.cheese.core.api

import com.hjq.window.EasyWindow
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.utils.PermissionsUtils
import net.codeocean.cheese.core.utils.PermissionsUtils.FLOATING

interface FloatingWindow {
    fun with(): EasyWindow<*>
    fun show(easyWindow: EasyWindow<*>)
    fun recycle(easyWindow: EasyWindow<*>)
    fun cancel(easyWindow: EasyWindow<*>)

    companion object:BaseEnv {
        fun requestPermission(timeout: Int): Boolean {
            return PermissionsUtils.requestPermission(FLOATING,timeout)
        }

        fun checkPermission(): Boolean {
            return PermissionsUtils.checkPermission(FLOATING)
        }
        fun recycleAll() {
            runOnUi {
                EasyWindow.recycleAll()
            }
        }

        fun cancelAll() {
            runOnUi {
                EasyWindow.cancelAll()
            }
        }
    }


}