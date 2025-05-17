package net.codeocean.cheese.core

import android.graphics.Rect
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class UiNodeCallBack {
    private var myCallback: IUiNodeCallBack? = null

    interface IUiNodeCallBack {
        fun callbackMethod(nodeType: node): Boolean
    }

    fun getCallBack(): IUiNodeCallBack? {
        return myCallback
    }

    fun setCallBack(callback: IUiNodeCallBack): UiNodeCallBack {
        myCallback = object : IUiNodeCallBack {
            override fun callbackMethod(nodeType: node): Boolean {
                return callback.callbackMethod(nodeType)
            }
        }
        return this
    }
    class node(val nodeType: AccessibilityNodeInfoCompat) {

        fun isEditable(): Boolean {
            return nodeType.isEditable
        }
        fun isDismissable(): Boolean {
            return nodeType.isDismissable
        }

        fun isChecked(): Boolean {
            return nodeType.isChecked
        }

        fun isEnabled(): Boolean {
            return nodeType.isEnabled
        }

        fun isPassword(): Boolean {
            return nodeType.isPassword
        }

        fun isScrollable(): Boolean {
            return nodeType.isScrollable
        }

        fun isGranularScrollingSupported(): Boolean {
            return nodeType.isGranularScrollingSupported
        }

        fun isTextSelectable(): Boolean {
            return nodeType.isTextSelectable
        }

        fun isImportantForAccessibility(): Boolean {
            return nodeType.isImportantForAccessibility
        }

        fun isAccessibilityDataSensitive(): Boolean {
            return nodeType.isAccessibilityDataSensitive
        }

        val text = nodeType.text
        fun getViewIdResourceName(): String? {
            return nodeType.getViewIdResourceName()
        }

        fun getWindowId(): Int {
            return nodeType.getWindowId()
        }

        fun getClassName(): CharSequence? {
            return nodeType.getClassName()
        }
        fun getChildren(i:Int): node {
            return node(nodeType.getChild(i))
        }

        fun getBoundsInWindow():Rect {
            val r =Rect()
            nodeType.getBoundsInWindow(r)
            return r
        }
        fun getBoundsInScreen(): Rect {
            val r =Rect()
            nodeType.getBoundsInScreen(r)
            return r
        }

        fun getRoleDescription(): CharSequence? {
            return nodeType.getRoleDescription()
        }

        fun getStateDescription(): CharSequence? {
            return nodeType.getStateDescription()
        }

        fun getContentDescription(): CharSequence? {
            return nodeType.getContentDescription()
        }
    }


}