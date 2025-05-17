package net.codeocean.cheese.core.runtime.debug.local
import net.codeocean.cheese.core.Script
import java.io.File

class LocalDebugController(private val script: Script) : Script by script {
    override fun run(workingDir: File) {
        script.run(workingDir)
    }

}