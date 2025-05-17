package net.codeocean.cheese.core.utils

object StringUtils {

    fun isFilePath(path: String): Boolean {
        val fileName = path.substringAfterLast("/")
        return fileName.contains(".")
    }
    fun isPathInStorage(str: String): Boolean {
        return str.startsWith("/storage/")
    }
    fun getRandomString(length: Int): String =
        (1..length)
            .map { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }
            .joinToString("")
}