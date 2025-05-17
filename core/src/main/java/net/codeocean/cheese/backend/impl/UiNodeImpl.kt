package net.codeocean.cheese.backend.impl

import android.annotation.SuppressLint
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import cn.vove7.andro_accessibility_api.requireBaseAccessibility
import cn.vove7.auto.core.api.findAllWith
import cn.vove7.auto.core.viewfinder.ConditionGroup
import cn.vove7.auto.core.viewnode.ViewNode
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.Action
import net.codeocean.cheese.core.CoreApp.Companion.globalVM
import net.codeocean.cheese.core.utils.AndroidPlatformReflectionUtils
import net.codeocean.cheese.core.UiNodeCallBack
import net.codeocean.cheese.core.api.UiNode
import net.codeocean.cheese.core.utils.Uix

class UiNodeImpl:UiNode {
    class _findAllWith : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.isNotEmpty()) { "传入参数数量错误" }
            val (a) = parameters
            return try {
                val id = when (val id = a) {
                    is UiNodeCallBack.IUiNodeCallBack -> id
                    else -> throw ClassCastException("a is neither a ConditionGroup nor a ViewNode")
                }
                findAllWith {
                    callbackMethod(id,it)
                }
            } catch (e: NumberFormatException) {
                XLog.e("查找错误")
                false
            }
        }
        fun callbackMethod(callBack: UiNodeCallBack.IUiNodeCallBack, nodeType: AccessibilityNodeInfoCompat): Boolean {
            return callBack.callbackMethod(UiNodeCallBack.node(nodeType))
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun forEachNode(mycallback: UiNodeCallBack.IUiNodeCallBack): List<ViewNode> {
      val view =Action.runAction(_findAllWith(), mycallback) as Array<ViewNode>
        return view.toList()
    }

}