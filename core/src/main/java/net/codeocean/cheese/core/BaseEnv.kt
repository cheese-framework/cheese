package net.codeocean.cheese.core

import android.app.Activity
import android.content.Context
import android.inputmethodservice.InputMethodService
import net.codeocean.cheese.core.service.AccessibilityService
import net.codeocean.cheese.core.service.KeyboardService

interface BaseEnv {

    val cx: Context
        get() = CoreEnv.envContext.context

    val act: Activity?
        get() = CoreEnv.envContext.activity

    val ims: KeyboardService?
        get() = CoreApp.globalVM.get()

    val error: CoreEnv.Error?
        get() = CoreEnv.error


}