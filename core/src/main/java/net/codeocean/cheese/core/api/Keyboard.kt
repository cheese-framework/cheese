package net.codeocean.cheese.core.api

interface Keyboard {
    fun input(text: String)
    fun delete()
    fun enter()
}