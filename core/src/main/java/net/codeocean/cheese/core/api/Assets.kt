package net.codeocean.cheese.core.api

interface Assets {
    fun read(path: String): String
    fun copy(path: String, destPath: String): Boolean
    fun isFolder(folderPath: String): Boolean
    fun isFile(filePath: String): Boolean
}