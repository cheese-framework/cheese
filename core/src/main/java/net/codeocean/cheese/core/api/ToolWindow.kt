package net.codeocean.cheese.core.api

import net.codeocean.cheese.core.window.Console
import net.codeocean.cheese.core.window.Logcat

interface ToolWindow {
    fun floatingConsole(): Console
    fun floatingLogcat(): Logcat
}