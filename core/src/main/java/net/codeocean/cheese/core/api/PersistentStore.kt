package net.codeocean.cheese.core.api

interface PersistentStore {
    fun save(name: String, key: String, value: Any)
    fun rm(name: String, key: String)
    fun get(name: String, key: String): Any?
}