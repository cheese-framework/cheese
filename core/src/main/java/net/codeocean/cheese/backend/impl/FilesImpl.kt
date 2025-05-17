package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.api.Files
import net.codeocean.cheese.core.utils.FilesUtils

object FilesImpl : Files {
    override fun read(path: String): Array<String>? {
        return FilesUtils.read(path)
    }

    override fun rm(path: String): Boolean {
        return FilesUtils.rm(path)
    }


    override fun create(path: String): Boolean {
        return FilesUtils.create(path)
    }


    override fun copy(sourceDirPath: String, destinationDirPath: String): Boolean {
        return FilesUtils.copy(sourceDirPath, destinationDirPath)
    }


    override fun readJson(filePath: String, keys: String): String? {
        return FilesUtils.readJson(filePath, keys)
    }


    override fun isFile(path: String): Boolean {
        return FilesUtils.isFile(path)
    }


    override fun isFolder(path: String): Boolean {
        return FilesUtils.isFolder(path)
    }


    override fun append(filePath: String, content: String): Boolean {
        return FilesUtils.append(filePath, content)
    }


    override fun write(filePath: String, content: String): Boolean {
        return FilesUtils.write(filePath, content)
    }


    override fun save(obj: Any, filePath: String): Boolean {
        return FilesUtils.save(obj, filePath)
    }
}