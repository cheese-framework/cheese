package net.codeocean.cheese.core.api

interface Zip {
    fun compress(srcFilePath: String, destFilePath: String, password: String): Boolean
    fun decompress(zipFilePath: String, destFilePath: String, password: String): Boolean
}