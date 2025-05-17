package  net.codeocean.cheese.core.service

import android.os.Process
import android.view.accessibility.AccessibilityEvent
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.auto.core.AppScope
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.CoreApp.Companion.globalVM

var accessibilityEvent: AccessibilityEvent? = null
var foregroundPkg: String? = null

class AccessibilityService : AccessibilityApi() {


    override val enableListenPageUpdate: Boolean = true

    override fun onCreate() {
        XLog.d("无障碍进程" + Process.myPid())
        baseService = this
        globalVM.add<android.accessibilityservice.AccessibilityService>(this)
        super.onCreate()
    }

    override fun onDestroy() {
        baseService = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            foregroundPkg= event.packageName?.toString()
        }

        accessibilityEvent = event
    }

    override fun onPageUpdate(currentScope: AppScope) {
        XLog.tag(TAG).i("onPageUpdate: $currentScope")
    }

    companion object {
        private const val TAG = "MyAccessibilityService"
    }

}