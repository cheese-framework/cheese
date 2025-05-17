package net.codeocean.cheese.core.utils

import android.graphics.Bitmap
import android.os.Environment
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FilesUtils {

    fun setExecutablePermissions(filePath: String) {
        val file = File(filePath)

        if (!file.exists()) {
            println("File does not exist.")
            return
        }

        try {
            // 设置文件的可执行权限
            if (file.setExecutable(true, false)) {
                println("Executable permissions set successfully.")
            } else {
                println("Failed to set executable permissions.")
            }
        } catch (e: Exception) {
            println("Error changing permissions: ${e.message}")
        }
    }

    fun findFile(directory: File, fileName: String): File? {
        val file = File(directory, fileName)
        if (file.exists()) {
            return file
        }

        val subDirectories = directory.listFiles { file -> file.isDirectory } ?: return null
        for (subDirectory in subDirectories) {
            val foundFile = findFile(subDirectory, fileName)
            if (foundFile != null) {
                return foundFile
            }
        }
        return null
    }

    fun findFile(startPath: String, fileName: String): String? {
        val startDir = File(startPath)
        return startDir.walkTopDown()
            .filter { it.name == fileName }
            .firstOrNull()
            ?.absolutePath
    }

    fun read(path: String): Array<String>? {
        if (isFile(path)) {
            return readFile(path)
        }
        return readFolder(path)
    }


    fun readByte(filePath: String): ByteArray? {
        return try {
            val file = File(filePath)
            file.readBytes()
        } catch (e: IOException) {
            println("Error reading binary file: ${e.message}")
            null
        }
    }


    fun rm(path: String): Boolean {
        if (isFile(path)) {
            return rmFile(path)
        }
        return rmFolder(path)
    }

    fun create(path: String): Boolean {
        if (StringUtils.isFilePath(path)) {
            return createFile(path)
        }
        return createFolder(path)
    }

    fun copy(sourceDirPath: String, destinationDirPath: String): Boolean {
        if (isFile(sourceDirPath)) {
            return copyFile(sourceDirPath, destinationDirPath)
        }
        return copyFolder(sourceDirPath, destinationDirPath)
    }

    fun save(obj: Any, filePath: String): Boolean {
        return when (obj) {
            is InputStream -> streamSaveSD(obj, filePath)
            is Bitmap -> bitmapSaveSD(obj, filePath)
            else -> false
        }
    }

    fun readJson(filePath: String, key: String): String? {
        val gson = Gson()
        return try {
            File(filePath).bufferedReader().use { reader ->
                val jsonContent = reader.readText()
                val jsonObject = gson.fromJson(jsonContent, Map::class.java)
                jsonObject[key]?.toString()
            }
        } catch (e: FileNotFoundException) {
            XLog.e("文件未找到: $filePath")
            null
        } catch (e: IOException) {
            XLog.e("读取文件时出错: $e")
            null
        } catch (e: Exception) {
            XLog.e("发生异常: $e")
            null
        }
    }

    fun isFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.isFile
    }

    fun isFolder(folderPath: String): Boolean {
        val directory = File(folderPath)
        return directory.exists() && directory.isDirectory
    }


    fun append(filePath: String, content: String): Boolean {
        var writer: BufferedWriter? = null
        return try {
            writer = BufferedWriter(FileWriter(filePath, true))
            writer.write(content + System.lineSeparator())
            writer.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun write(filePath: String, content: String): Boolean {
        var writer: FileWriter? = null
        return try {
            writer = FileWriter(filePath, false)
            writer.write(content)
            writer.flush()
            true
        } catch (e:IOException) {
            e.printStackTrace()
            false
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun copyFolder(sourceDirPath: String, destinationDirPath: String): Boolean {
        val sourceDir = File(sourceDirPath)
        val destinationDir = File(destinationDirPath)

        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            XLog.e("Source directory does not exist or is not a directory.")
            return false
        }

        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        val files = sourceDir.listFiles() ?: return true

        for (file in files) {
            val sourceFilePath = file.absolutePath
            val destinationFilePath = "${destinationDir.absolutePath}/${file.name}"
            if (file.isDirectory) {
                copyFolder(sourceFilePath, destinationFilePath)
            } else {
                copyFile(sourceFilePath, destinationFilePath)
            }
        }
        return true
    }

    private fun copyFile(sourceFilePath: String, destinationFilePath: String): Boolean {
        val sourceFile = File(sourceFilePath)
        val destinationFile = File(destinationFilePath)

        if (!sourceFile.exists() || !sourceFile.isFile) {
            XLog.e("Source file does not exist or is not a file.")
            return false
        }

        val destinationDirectory = destinationFile.parentFile
        if (!destinationDirectory!!.exists()) {
            if (!destinationDirectory.mkdirs()) {
                XLog.e("Failed to create destination directory.")
                return false
            }
        }

        try {
            FileInputStream(sourceFile).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    private fun createFolder(folderPath: String): Boolean {
        val folder = File(folderPath)
        if (folder.exists() && folder.isDirectory()) {
            return true
        }
        return folder.mkdirs()
    }

    private fun createFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    private fun readFolder(folderPath: String): Array<String>? {
        val fileNames = mutableListOf<String>()
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            return null
        }
        listFilesRecursively(folder, fileNames)
        return fileNames.toTypedArray()
    }

    private fun readFile(filePath: String): Array<String>? {
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }

        return try {
            val linesList = mutableListOf<String>()
            FileReader(file).buffered().use { reader ->
                reader.forEachLine { line ->
                    linesList.add(line)
                }
            }
            linesList.toTypedArray()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun rmFolder(folderPath: String): Boolean {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            return false
        }
        folder.listFiles()?.forEach { file ->
            if (file.isFile) {
                if (!file.delete()) {
                    return false
                }
            } else if (file.isDirectory) {
                if (!rmFolder(file.absolutePath)) {
                    return false
                }
            }
        }
        return folder.delete()
    }

    private fun rmFile(filePath: String): Boolean {
        val file = File(filePath)
        return try {
            file.delete()
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }

    private fun listFilesRecursively(folder: File, fileNames: MutableList<String>) {
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile()) {
                    fileNames.add(file.getName())
                } else if (file.isDirectory()) {
                    listFilesRecursively(file, fileNames)
                }
            }
        }
    }

    fun streamSaveSD(inputStream: InputStream, filePath: String): Boolean {
        try {
            val outputStream: OutputStream = FileOutputStream(filePath)
            val buffer = ByteArray(4096)
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            return true
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun bitmapSaveSD(bitmap: Bitmap, filePath: String): Boolean {

        var result = false
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            var fos: FileOutputStream? = null
            try {
                val file = File(filePath)
                val parentFile = file.getParentFile()
                if (!parentFile!!.exists()) {
                    parentFile.mkdirs()
                }
                fos = FileOutputStream(file)
                result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (fos != null) {
                        fos.flush()
                        fos.close()
                    }
                } catch (e: java.io.IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

}