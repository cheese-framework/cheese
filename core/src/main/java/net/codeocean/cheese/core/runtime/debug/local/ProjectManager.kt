package net.codeocean.cheese.core.runtime.debug.local

import android.content.ComponentName
import android.content.Intent
import com.elvishew.xlog.XLog
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_ACTIVITY_NAME
import com.termux.shared.termux.TermuxConstants.TERMUX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH
import com.termux.shared.termux.TermuxConstants.TERMUX_PACKAGE_NAME
import net.codeocean.cheese.backend.impl.PathImpl
import net.codeocean.cheese.backend.impl.TermuxComm
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.utils.ZipUtils

import java.io.File
import java.io.IOException

object ProjectManager {
    data class Project(
        val id: Int,
        val name: String,
        val packageName: String,
        val language: String,
        val ui: String,
        val path: String = ""
    )

    /**
     * 获取项目目录下所有子文件夹的名称和路径
     * @return List<Pair<项目名, 绝对路径>>
     */
    fun getProjectList(): List<Pair<String, String>> {
        return try {
            val projectDir = PathImpl.SD_PROJECT_DIRECTORY
            when {
                !projectDir.exists() -> {
                    projectDir.mkdirs()
                    emptyList()
                }

                !projectDir.isDirectory -> {
                    emptyList()
                }

                else -> {
                    projectDir.listFiles()
                        ?.filter { it.isDirectory }
                        ?.map { folder ->
                            folder.name to folder.absolutePath
                        }
                        ?: emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class DemoEntry(val name: String, val path: String)
    data class DemoGroup(val name: String, val children: List<DemoEntry>)

    fun getDemoList(): List<DemoGroup> {
        val demoDir = PathImpl.SD_DEMO_DIRECTORY
        val groups = mutableListOf<DemoGroup>()

        if (!demoDir.exists() || !demoDir.isDirectory) return emptyList()

        demoDir.listFiles()?.filter { it.isDirectory }?.forEach { topLevel ->
            val children = topLevel.listFiles()
                ?.filter { it.isDirectory }
                ?.map { DemoEntry(it.name, it.absolutePath) }
                ?: emptyList()

            groups.add(DemoGroup(topLevel.name, children))
        }

        return groups
    }


    fun readMain(name: String): String {
        return File(PathImpl.SD_PROJECT_DIRECTORY, "$name/src/main/js/main.js").readText()
    }

    fun readDemoMain(name: String): String {
        return File(PathImpl.SD_DEMO_DIRECTORY, "$name/main.js").readText()
    }


    fun createFile(name: String, parentPath: String, fileName: String) {
        File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath/$fileName").createNewFile()
    }


    fun rename(name: String, parentPath: String, newName: String) {
        val originalFile = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath")

        if (!originalFile.exists()) {
            XLog.e("文件或文件夹不存在: ${originalFile.absolutePath}")
            return
        }
        val newFile = if (originalFile.isFile) {

            val extension = originalFile.extension
            val newFileName = if (extension.isNotEmpty()) "$newName.$extension" else newName
            File(originalFile.parentFile, newFileName)
        } else {

            File(originalFile.parentFile, newName)
        }

        if (!originalFile.renameTo(newFile)) {
            XLog.e("重命名失败！可能原因：权限不足、文件被占用或目标已存在。")
        }
    }


    fun cut(name: String, parentPath: String, target: String) {
        val sourceFile = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath")
        val targetDir = File(target)

        if (!sourceFile.exists()) {
            XLog.e("源文件或文件夹不存在: ${sourceFile.absolutePath}")
            return
        }

        try {
            if (sourceFile.isFile) {
                // 复制文件到目标路径
                val targetFile = File(targetDir, sourceFile.name)
                sourceFile.copyTo(targetFile, overwrite = true)
                // 删除原文件
                if (!sourceFile.delete()) {
                    XLog.e("文件剪切失败: 无法删除原文件")
                    targetFile.delete() // 回滚：删除已复制的文件
                }
            } else if (sourceFile.isDirectory) {
                // 复制文件夹到目标路径
                val targetFolder = File(targetDir, sourceFile.name)
                sourceFile.copyRecursively(targetFolder, overwrite = true)
                // 删除原文件夹
                if (!sourceFile.deleteRecursively()) {
                    XLog.e("文件夹剪切失败: 无法删除原文件夹")
                    targetFolder.deleteRecursively() // 回滚：删除已复制的文件夹
                }
            }
        } catch (e: IOException) {
            XLog.e("剪切失败: ${e.message}")
        }
    }

    fun copy(name: String, parentPath: String, target: String) {
        val sourceFile = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath")
        val targetDir = File(target)

        if (!sourceFile.exists()) {
            XLog.e("源文件或文件夹不存在: ${sourceFile.absolutePath}")
            return
        }

        try {
            if (sourceFile.isFile) {
                // 复制文件
                val targetFile = File(targetDir, sourceFile.name)
                sourceFile.copyTo(targetFile, overwrite = true)
            } else if (sourceFile.isDirectory) {
                // 复制文件夹（递归）
                val targetFile = File(targetDir, sourceFile.name)
                sourceFile.copyRecursively(targetFile, overwrite = true)
            }
        } catch (e: IOException) {
            XLog.e("复制失败: ${e.message}")
        }
    }


    fun import(name: String, parentPath: String, source: String) {
        val targetDir = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath")
        val sourceFile = File(source)
        if (!sourceFile.exists()) {
            XLog.e("源文件或文件夹不存在: ${sourceFile.absolutePath}")
            return
        }
        try {
            if (sourceFile.isFile) {
                // 复制文件
                val targetFile = File(targetDir, sourceFile.name)
                sourceFile.copyTo(targetFile, overwrite = true)

            } else if (sourceFile.isDirectory) {
                // 复制文件夹（递归）
                val targetFile = File(targetDir, sourceFile.name)
                sourceFile.copyRecursively(targetFile, overwrite = true)

            }
        } catch (e: IOException) {
            XLog.e("复制失败: ${e.message}")
        }

    }

    fun rm(name: String, parentPath: String) {
        val file = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath")

        if (file.exists()) {
            if (file.isFile) {
                // 如果是文件，直接删除
                if (!file.delete()) {
                    XLog.e("文件删除失败: ${file.absolutePath}")
                }
            } else if (file.isDirectory) {
                // 如果是文件夹，递归删除所有内容
                if (!file.deleteRecursively()) {
                    XLog.e(("文件夹删除失败: ${file.absolutePath}"))
                }
            }
        } else {
            XLog.e("文件或文件夹不存在: ${file.absolutePath}")
        }
    }

    fun createFolder(name: String, parentPath: String, folderName: String) {
        val folder = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath/$folderName")
        XLog.e(folder.absolutePath)
        if (!folder.exists()) {

            folder.mkdirs()  // 创建多级目录
        }
    }


    fun getProjectPath(name: String): String {
        return File(PathImpl.SD_PROJECT_DIRECTORY, name).absolutePath
    }

    fun getDemoPath(name: String): String {
        return File(PathImpl.SD_DEMO_DIRECTORY, name).absolutePath
    }


    fun renameProject(oldPath: String, newName: String): Boolean {
        val oldFile = File(oldPath)
        val parentDir = oldFile.parent ?: return false // 获取父目录路径
        val newFile = File(parentDir, newName)

        return oldFile.renameTo(newFile)
    }

    fun deleteProject(path: String) {
        File(path).deleteRecursively()
    }


    fun copyProjectFiles(sourceDir: File, targetDir: File): Boolean {
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            XLog.e("源目录不存在或不是文件夹: ${sourceDir.absolutePath}")
            return false
        }

        File(PathImpl.SD_DEMO_DIRECTORY, "cheese.toml").copyTo(
            File(
                PathImpl.WORKING_DIRECTORY,
                "cheese.toml"
            ), true
        )

        File(
            PathImpl.SD_ROOT_DIRECTORY,
            "sdk/components/project/node_modules"
        ).copyRecursively(File(PathImpl.WORKING_DIRECTORY, "node_modules"), true)

        var success = true

        // 拷贝 assets 文件夹
        val assetsDir = File(sourceDir, "assets")
        if (assetsDir.exists() && assetsDir.isDirectory) {
            val targetAssetsDir = File(targetDir, "assets")
            success = success && copyDirectoryRecursively(assetsDir, targetAssetsDir)
        }

        // 拷贝 ui 文件夹
        val uiDir = File(sourceDir, "ui/dist")
        if (uiDir.exists() && uiDir.isDirectory) {
            val targetUiDir = File(targetDir, "ui")
            success = success && copyDirectoryRecursively(uiDir, targetUiDir)
        }

        // 拷贝所有 .js 文件到 js 文件夹
        val jsFiles =
            sourceDir.listFiles { file -> file.isFile && file.extension == "js" } ?: emptyArray()
        if (jsFiles.isNotEmpty()) {
            val targetJsDir = File(targetDir, "js")
            if (!targetJsDir.exists()) targetJsDir.mkdirs()

            jsFiles.forEach { jsFile ->
                val targetFile = File(targetJsDir, jsFile.name)
                try {
                    jsFile.copyTo(targetFile, overwrite = true)
                } catch (e: IOException) {
                    XLog.e("复制失败: ${jsFile.absolutePath} -> ${targetFile.absolutePath}")
                    success = false
                }
            }
        }

        return success
    }

    fun copyDirectoryRecursively(source: File, target: File): Boolean {
        if (!target.exists()) target.mkdirs()

        var success = true

        source.listFiles()?.forEach { file ->
            val dest = File(target, file.name)
            if (file.isDirectory) {
                success = success && copyDirectoryRecursively(file, dest)
            } else {
                try {
                    file.copyTo(dest, overwrite = true)
                } catch (e: IOException) {
                    XLog.e("复制失败: ${file.absolutePath} -> ${dest.absolutePath}")
                    success = false
                }
            }
        }

        return success
    }

    fun clearProjectRuntimeDirectory() {

        PathImpl.MAIN_DIRECTORY.deleteRecursively()

    }

    fun build(path: String, c: (t: Boolean) -> Unit) {
        val termux = CoreEnv.envContext.context.let { TermuxComm(it) }
        val command =
            "java -Dfile.encoding=UTF-8 -jar ${PathImpl.SDK_DIRECTORY.path}/lib/core.jar -build -baseDir ${path} -sdkPath ${PathImpl.SDK_DIRECTORY.path} -u"

        termux.execute(command) { stdout, stderr, code ->
            XLog.i("STDOUT: $stdout\nSTDERR: $stderr")
            c(true)
        }
    }

    fun getLastDirName(path: String): String? {
        val file = File(path.trimEnd('/'))
        return if (file.isDirectory || !file.exists()) {
            file.name
        } else {
            file.parentFile?.name
        }
    }

    fun runUi(mode: Int, path: String, c: (t: Boolean) -> Unit) {
        clearProjectRuntimeDirectory()
        if (mode == 0) {
            val source = File(path) // 你的源目录
            val target = PathImpl.MAIN_DIRECTORY
            if (copyProjectFiles(source, target)) {
                c(true)
            } else {
                c(false)
            }

        } else {
            val termux = CoreEnv.envContext.context.let { TermuxComm(it) }
            val command =
                "java -Dfile.encoding=UTF-8 -jar ${PathImpl.SDK_DIRECTORY.path}/lib/core.jar -runUi -baseDir ${path} -sdkPath ${PathImpl.SDK_DIRECTORY.path}"
            termux.execute(command) { stdout, stderr, code ->
                XLog.i("STDOUT: $stdout\nSTDERR: $stderr")
                val targetDir: String = PathImpl.WORKING_DIRECTORY.path
                val debugZip = File("${targetDir}/debug.zip").apply {
                    if (exists()) delete()
                }
                File("$path/build/debug.zip").copyTo(debugZip)

                if (debugZip.exists()) {
                    ZipUtils.decompress(
                        debugZip.path,
                        targetDir,
                        ""
                    )
                    c(true)
                } else {
                    c(false)
                }

            }
        }


    }

    fun openTermux(name: String, parentPath: String) {

        var file = File(PathImpl.SD_PROJECT_DIRECTORY, "$name/$parentPath")


        if (file.path.contains("src/main/ui")) {
            file = File(TERMUX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH, "/ui/$name")
        }

        if (file.path.contains(" /storage/emulated/0/cheese")) {
            file = File(PathImpl.SD_PROJECT_DIRECTORY, name)
        }
        XLog.e(file.path)

        val intent = Intent().apply {
            component = ComponentName(
                TERMUX_PACKAGE_NAME,
                TERMUX_ACTIVITY_NAME
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(
                "cmd",
                "cd ${file.path}"
            ) // 你要附带的数据
        }
        CoreEnv.envContext.context.startActivity(intent)
    }


    fun run(mode: Int, path: String, c: (t: Boolean) -> Unit) {
        clearProjectRuntimeDirectory()
        if (mode == 0) {
            val source = File(path) // 你的源目录
            val target = PathImpl.MAIN_DIRECTORY
            if (copyProjectFiles(source, target)) {
                c(true)
            } else {
                c(false)
            }

        } else {
            val termux = CoreEnv.envContext.context.let { TermuxComm(it) }

            termux.execute("""java -Dfile.encoding=UTF-8 -jar ${PathImpl.SDK_DIRECTORY.path}/lib/core.jar -runCode -baseDir ${path} -sdkPath ${PathImpl.SDK_DIRECTORY.path}""".trim()) { stdout, stderr, code ->
                XLog.i("Exit $code\nOutput:\n$stdout\nError:\n$stderr")
                val targetDir: String = PathImpl.WORKING_DIRECTORY.path
                val debugZip = File("${targetDir}/debug.zip").apply {
                    if (exists()) delete()
                }
                File("$path/build/debug.zip").copyTo(debugZip)

                if (debugZip.exists()) {
                    ZipUtils.decompress(
                        debugZip.path,
                        targetDir,
                        ""
                    )
                    c(true)
                } else {
                    c(false)
                }
            }


        }


    }


    fun createProject(config: Project, c: (t: Boolean) -> Unit) {
        return try {

            val termux = CoreEnv.envContext.context.let { TermuxComm(it) }

            termux.execute("""java -Dfile.encoding=UTF-8 -jar ${PathImpl.SDK_DIRECTORY.path}/lib/core.jar -create -c "{name:${config.name},pkg:${config.packageName},bindings:${config.language},ui:${config.ui}}" -sdkPath ${PathImpl.SDK_DIRECTORY.path} -o ${PathImpl.SD_PROJECT_DIRECTORY}/${config.name} && mkdir -p /data/user/0/com.termux/ui/${config.name} && cp -r ${PathImpl.SD_PROJECT_DIRECTORY}/${config.name}/src/main/ui/. /data/user/0/com.termux/ui/${config.name} && cd /data/user/0/com.termux/ui/${config.name} && cnpm install """.trim()) { stdout, stderr, code ->
                XLog.i("Exit $code\nOutput:\n$stdout\nError:\n$stderr")

                c(true)

            }

            c(false)
        } catch (e: Exception) {
            e.printStackTrace()
            c(false)
        }
    }
}

