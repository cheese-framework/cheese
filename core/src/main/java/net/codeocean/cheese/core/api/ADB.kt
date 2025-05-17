package net.codeocean.cheese.core.api

interface ADB {
    fun exec(command: String): String?
}