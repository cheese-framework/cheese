package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.api.ToolWindow
import net.codeocean.cheese.core.window.Console
import net.codeocean.cheese.core.window.Logcat

object ToolWindowImpl:ToolWindow {
    override fun floatingConsole(): Console {
        return Console(baseEnv = CoreEnv.envContext)
    }

    override fun floatingLogcat(): Logcat {
       return Logcat(baseEnv = CoreEnv.envContext,null)
    }
}