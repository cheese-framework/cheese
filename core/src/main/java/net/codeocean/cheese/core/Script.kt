package net.codeocean.cheese.core

import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.core.api.Path

import java.io.File

interface Script {

    fun run(workingDir: File = PathImpl.WORKING_DIRECTORY)
    fun exit()
    fun convertNativeObjectToMap(nativeObj: Any): HashMap<Any, Any>
    fun convertNativeArrayToList (value: Any) : ArrayList<Any>
}