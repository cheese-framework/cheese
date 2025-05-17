package net.codeocean.cheese.core.api

import android.app.Activity
import android.content.Context
import net.codeocean.cheese.core.service.AccessibilityService

interface Env {

    val context: Context
    val version: String
    val settings: MutableMap<String, Any>
    val activity: Activity?



}