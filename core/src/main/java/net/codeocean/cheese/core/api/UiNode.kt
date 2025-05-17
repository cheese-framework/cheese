package net.codeocean.cheese.core.api

import cn.vove7.auto.core.viewnode.ViewNode
import net.codeocean.cheese.core.CoreApp.Companion.globalVM
import net.codeocean.cheese.core.UiNodeCallBack
import net.codeocean.cheese.core.utils.AndroidPlatformReflectionUtils
import net.codeocean.cheese.core.utils.Uix

interface UiNode {

    fun forEachNode(mycallback: UiNodeCallBack.IUiNodeCallBack): List<ViewNode>

    companion object {
        fun getUi(path: String): Boolean {
            Uix.saveCurrentUiHierarchyToFile(globalVM.get<android.accessibilityservice.AccessibilityService>(),path)
            return true
        }
        fun clearNodeCache(): Boolean {
            return AndroidPlatformReflectionUtils.clearAccessibilityCache()
        }
    }

}