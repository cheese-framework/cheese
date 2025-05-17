package net.codeocean.cheese.backend.impl

import android.os.Build
import net.codeocean.cheese.core.api.Keys

object KeysImpl:Keys {
    override fun home(): Boolean {
        return cn.vove7.auto.core.api.home()
    }

    override fun back(): Boolean {
        return cn.vove7.auto.core.api.back()
    }

    override fun quickSettings(): Boolean {
        return  cn.vove7.auto.core.api.quickSettings()
    }

    override fun powerDialog(): Boolean {
        return cn.vove7.auto.core.api.powerDialog()
    }

    override fun pullNotificationBar(): Boolean {
        return cn.vove7.auto.core.api.pullNotificationBar()
    }

    override fun recents(): Boolean {
        return cn.vove7.auto.core.api.recents()
    }

    override fun lockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cn.vove7.auto.core.api.lockScreen()
        } else false
    }

    override fun screenShot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cn.vove7.auto.core.api.screenShot()
        } else false
    }

    override fun splitScreen(): Boolean {
        return  cn.vove7.auto.core.api.splitScreen()
    }
}