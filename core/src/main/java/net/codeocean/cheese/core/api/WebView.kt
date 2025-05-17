package net.codeocean.cheese.core.api

import net.codeocean.cheese.core.IAction

interface WebView {
    fun inject(iWebView: IAction)
    fun runWebView(id:String)
    fun document(methodName: String, vararg args: Any?): Any?
    fun window(methodName: String, vararg args: Any?): Any?
}