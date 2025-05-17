package net.codeocean.cheese.core.api

interface Files {
    fun read(path: String): Array<String>?
    fun rm(path: String): Boolean
    fun create(path: String): Boolean
    fun copy(sourceDirPath: String, destinationDirPath: String): Boolean
    fun readJson(filePath: String, keys: String): String?
    fun isFile(path: String): Boolean
    fun isFolder(path: String): Boolean
    fun append(filePath: String, content: String): Boolean
    fun write(filePath: String, content: String): Boolean
    fun save(obj: Any, filePath: String): Boolean
}