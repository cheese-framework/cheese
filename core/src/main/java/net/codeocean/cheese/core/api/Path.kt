package net.codeocean.cheese.core.api



import android.os.Environment
import net.codeocean.cheese.core.CoreApp
import net.codeocean.cheese.core.CoreEnv
import java.io.File
private operator fun File.div(child: String): File = File(this, child)
interface Path {
    val ROOT_DIRECTORY: File;
    val LOG_DIRECTORY: File get() = ROOT_DIRECTORY / "log"
    val WORKING_DIRECTORY: File get() = ROOT_DIRECTORY / if (!CoreEnv.runTime.isDebugMode) "release" else "debug"
    val MAIN_DIRECTORY: File get() = WORKING_DIRECTORY / "main"
    val UI_DIRECTORY: File get() = MAIN_DIRECTORY / "ui"
    val ASSETS_DIRECTORY: File get() = MAIN_DIRECTORY / "assets"
    val JS_DIRECTORY: File get() = MAIN_DIRECTORY / "js"
    val SD_ROOT_DIRECTORY : File get() = Environment.getExternalStorageDirectory() / "cheese"
    val SD_PROJECT_DIRECTORY : File get() = SD_ROOT_DIRECTORY / "project"
    val SD_DEMO_DIRECTORY : File get() = SD_ROOT_DIRECTORY / "demo"
    val SDK_DIRECTORY : File get() = SD_ROOT_DIRECTORY / "sdk"

}