package net.codeocean.cheese.backend.impl
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.CoreApp
import net.codeocean.cheese.core.api.Path
import java.io.File

 object PathImpl : Path,BaseEnv {
    override val ROOT_DIRECTORY: File
        get() =  cx.getExternalFilesDir("")?.parentFile
    ?: throw IllegalStateException("Root directory not found!")


}