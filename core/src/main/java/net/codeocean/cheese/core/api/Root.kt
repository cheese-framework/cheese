package net.codeocean.cheese.core.api

interface Root {
    fun exec(command: String): String
    fun requestPermission(timeout:Int): Boolean
    fun checkPermission(): Boolean
}