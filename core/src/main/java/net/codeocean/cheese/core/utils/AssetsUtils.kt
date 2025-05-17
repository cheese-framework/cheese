package net.codeocean.cheese.core.utils

import android.content.Context
import android.util.Log
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object AssetsUtils {
    fun readJson(context: Context, fileName: String, key: String): String? {
        val gson = Gson()
        return try {
            context.assets.open(fileName).bufferedReader().use { reader ->
                val jsonContent = reader.readText()
                val jsonObject = gson.fromJson(jsonContent, Map::class.java)
                jsonObject[key]?.toString()
            }
        } catch (e: FileNotFoundException) {
            println("文件未找到: $fileName")
            null
        } catch (e: IOException) {
            println("读取 assets 文件时出错: $e")
            null
        } catch (e: Exception) {
            println("解析 JSON 时发生异常: $e")
            null
        }
    }


    fun copyFileToSD(cx: Context, fileName: String, destPath: String): Boolean {
        val assetManager = cx.assets
        return try {
            val inputStream = assetManager.open(fileName)
            val destFile = if (File(destPath).isDirectory) {
                val actualFileName = fileName.substringAfterLast("/")
                File(destPath, actualFileName)
            } else {
                File(destPath)
            }
            destFile.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }

            if (!destFile.exists()) {
                destFile.createNewFile()
            }


            FileOutputStream(destFile).use { outputStream ->
                inputStream.use { input ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }

            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun copyFolderToSD(cx: Context, sourceFolder: String, destFolder: String): Boolean {
        return try {
            val assetManager = cx.assets
            val files = assetManager.list(sourceFolder) ?: return false
            if (files.isEmpty()) {
                return false
            }

            val targetFolderName = File(sourceFolder).name
            val targetFolder = File(destFolder, targetFolderName).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            for (filename in files) {
                val fullPath = "$sourceFolder/$filename"

                if (isFolder(cx,fullPath)) {
                    XLog.d("CopyFolder", "Copying folder: $fullPath")
                    // 递归调用复制子文件夹
                    copyFolderToSD(cx, fullPath, targetFolder.absolutePath)
                } else {
                    XLog.d(
                        "CopyFolder",
                        "Copying file: $fullPath to ${File(targetFolder, filename).absolutePath}"
                    )
                    assetManager.open(fullPath).use { inputStream ->
                        File(targetFolder, filename).outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }


    fun isFolder(cx: Context, folderPath: String): Boolean {
        val assetManager = cx.assets
        try {
            val files = assetManager.list(folderPath)
            return !files.isNullOrEmpty()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }


    fun isFile(cx: Context, filePath: String): Boolean {
        return try {
            val inputStream = cx.assets.open(filePath)
            inputStream.close()
            true
        } catch (e: IOException) {
            false
        }
    }
}