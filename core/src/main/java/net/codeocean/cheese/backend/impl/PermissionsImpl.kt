package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.api.Permissions
import net.codeocean.cheese.core.utils.PermissionsUtils

object PermissionsImpl: Permissions {

   override fun requestPermission(permission: Int,timeout:Int):Boolean {
        return PermissionsUtils.requestPermission(permission,timeout)
    }

    override fun checkPermission(permission: Int):Boolean {
        return PermissionsUtils.checkPermission(permission)
    }


}