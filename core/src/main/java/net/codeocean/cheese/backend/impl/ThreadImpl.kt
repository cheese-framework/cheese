package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.api.Thread
import net.codeocean.cheese.core.runtime.debug.remote.DebugController
import net.codeocean.cheese.core.utils.StringUtils

class ThreadImpl : Thread {
    var id = ""
    override fun create(runnable: Runnable): Thread {
        id = "${this.javaClass.simpleName}@${StringUtils.getRandomString(6)}"
        val executor = DebugController.createNamedThreadPool()
        CoreEnv.executorMap[id] = executor.submit(runnable)
        return this
    }

    override fun getID(): String {
        return id
    }

    override fun exit() {
        CoreEnv.executorMap[id]?.cancel(true)
    }
}