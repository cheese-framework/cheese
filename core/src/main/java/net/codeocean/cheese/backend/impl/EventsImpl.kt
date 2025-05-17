package net.codeocean.cheese.backend.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.elvishew.xlog.XLog
import net.codeocean.cheese.core.BaseEnv
import net.codeocean.cheese.core.CoreEnv

import net.codeocean.cheese.core.api.Events
import net.codeocean.cheese.core.utils.EventsUtils

object EventsImpl: Events, BaseEnv {

    private var receiver: EventsUtils? = null
    private var isReceiverRegistered = false
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
   override fun observeKey(eventCallback: (String) -> Unit) {
        if (isReceiverRegistered) return  // 如果已经注册了接收器，直接返回
        receiver = EventsUtils(eventCallback)
        // 创建 IntentFilter 并添加音量键改变的 action
        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cx.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            cx.registerReceiver(receiver, filter)
        }
        isReceiverRegistered = true  // 标记接收器已注册
    }

    override fun stop() {
        receiver?.let {
            cx.unregisterReceiver(it)
            isReceiverRegistered = false  // 取消注册标志
            receiver = null  // 清除 receiver 对象，防止重复注销
        } ?: run {
            XLog.d("Receiver", "Receiver is already null or not registered.")
        }
    }

    override val cx: Context
        get() = CoreEnv.envContext.context

}