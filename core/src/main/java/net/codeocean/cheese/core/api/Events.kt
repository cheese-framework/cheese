package net.codeocean.cheese.core.api

interface Events {
    fun observeKey(eventCallback: (String) -> Unit)
    fun stop()
}