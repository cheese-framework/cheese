package net.codeocean.cheese.core.api

interface Thread {
    fun create(runnable: Runnable): Thread
    fun getID(): String
    fun exit()
}