package net.codeocean.cheese.backend.impl

import com.hjq.window.EasyWindow

import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.Misc.runOnUi
import net.codeocean.cheese.core.api.FloatingWindow
import net.codeocean.cheese.core.utils.PermissionsUtils
import net.codeocean.cheese.core.utils.PermissionsUtils.FLOATING

class FloatingWindowImpl : FloatingWindow,BaseEnv {
    override fun with(): EasyWindow<*> {
        return EasyWindow.with(act)
    }

    override fun show(easyWindow: EasyWindow<*>) {
        runOnUi {
            easyWindow.show()
        }
    }

    override fun recycle(easyWindow: EasyWindow<*>) {
        runOnUi {
            easyWindow.recycle()
        }
    }

    override fun cancel(easyWindow: EasyWindow<*>) {
        runOnUi {
            easyWindow.cancel()
        }
    }


}