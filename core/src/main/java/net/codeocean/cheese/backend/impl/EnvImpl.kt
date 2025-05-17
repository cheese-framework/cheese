package net.codeocean.cheese.backend.impl

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.api.Assets

import net.codeocean.cheese.core.api.Env
import net.codeocean.cheese.core.service.AccessibilityService
import java.util.concurrent.ConcurrentHashMap

object EnvImpl : Env, BaseEnv {
    override val context: Context
        get() = cx
    override val version: String
        get() = "0.0.1"
    override val settings: MutableMap<String, Any>
        get() = ConcurrentHashMap<String, Any>().apply {}
    override val activity: Activity?
        get() = act

}