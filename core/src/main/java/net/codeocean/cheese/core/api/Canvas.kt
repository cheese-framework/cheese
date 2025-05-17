package net.codeocean.cheese.core.api

import com.hjq.window.EasyWindow

interface Canvas {
    fun drawRectOnScreen(vararg elements: Any): EasyWindow<*>?
     fun drawPointOnScreen(vararg elements: Any): EasyWindow<*>?
}