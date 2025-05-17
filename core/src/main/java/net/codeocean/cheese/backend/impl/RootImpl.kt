package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.api.Root
import net.codeocean.cheese.core.utils.PermissionsUtils

object RootImpl :Root {
    override fun exec(command: String): String {
        val result = StringBuilder()
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val inputStream = process.inputStream
            val errorStream = process.errorStream
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach {
                    result.append(it).append("\n")
                }
            }
            errorStream.bufferedReader().useLines { lines ->
                lines.forEach {
                    result.append(it).append("\n")
                }
            }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                println("Command '$command' failed with exit code $exitCode")
            }
            inputStream.close()
            errorStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result.toString()
    }

    override fun requestPermission(timeout: Int): Boolean {
        return PermissionsUtils.requestPermission(PermissionsUtils.ROOT, timeout)
    }

    override fun checkPermission(): Boolean {
        return PermissionsUtils.checkPermission(PermissionsUtils.ROOT)
    }
}