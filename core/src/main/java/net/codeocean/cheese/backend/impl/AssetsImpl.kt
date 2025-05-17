package net.codeocean.cheese.backend.impl

import android.content.Context

import net.codeocean.cheese.core.api.Assets
import net.codeocean.cheese.core.api.Path
import net.codeocean.cheese.core.utils.FilesUtils

object AssetsImpl : Assets {
    override fun read(path: String): String {
        val newPath = if (path.startsWith("/")) { path.substring(1) } else { path }
        val filePath = "${PathImpl.ASSETS_DIRECTORY}/$newPath"
        return FilesUtils.read(filePath)?.joinToString(" ") ?: ""
    }

    override fun copy(path: String, destPath: String): Boolean {
        val newPath = if (path.startsWith("/")) { path.substring(1) } else { path }
        val filePath = "${PathImpl.ASSETS_DIRECTORY}/$newPath"
        return FilesUtils.copy(filePath, destPath)
    }

    override fun isFolder(folderPath: String): Boolean {
        val newPath = if (folderPath.startsWith("/")) {
            folderPath.substring(1)
        } else {
            folderPath
        }
        return FilesUtils.isFolder("${PathImpl.ASSETS_DIRECTORY}/$newPath")
    }

    override fun isFile(filePath: String): Boolean {
        val newPath = if (filePath.startsWith("/")) {
            filePath.substring(1)
        } else {
            filePath
        }
        return FilesUtils.isFile("${PathImpl.ASSETS_DIRECTORY}/$newPath")
    }
}