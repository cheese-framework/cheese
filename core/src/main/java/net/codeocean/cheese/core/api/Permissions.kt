package net.codeocean.cheese.core.api

interface Permissions {
    val ACCESSIBILITY: Int get() { return 1 }
    val FLOATING: Int get() { return 2 }
    val RECORDSCREEN: Int get() { return 3 }
    val ROOT: Int get() { return 4 }

    fun requestPermission(permission: Int,timeout:Int):Boolean
    fun checkPermission(permission: Int):Boolean
}