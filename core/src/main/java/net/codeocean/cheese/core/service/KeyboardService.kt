package net.codeocean.cheese.core.service

import android.inputmethodservice.InputMethodService
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.CoreApp.Companion.globalVM

class KeyboardService : InputMethodService(){


    override fun onCreate() {
        super.onCreate()
        XLog.i("初始化输入法")
        globalVM.add(this);
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}