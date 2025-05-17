package net.codeocean.cheese.core.utils

import android.os.Looper
import android.os.Process
import com.elvishew.xlog.XLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.codeocean.cheese.core.CoreEnv

import net.codeocean.cheese.core.Logger
import net.codeocean.cheese.core.exception.ScriptInterruptedException
import net.codeocean.cheese.core.runtime.debug.remote.DebugController
import net.codeocean.cheese.core.window.Logcat
import java.lang.Thread.sleep


object ViewLogger {
    var logger: Logger? = null
    fun setViewLogger(logger: Logger){
        this.logger=logger
    }

    fun addLog(msg:String) {
        logger?.addLog(msg)
    }
}


object ScriptLogger {

    private fun logMessage(level: String, msg: String) {
        if (!msg.contains("ScriptInterruptedException")) {
            try {
                if (!msg.contains("运行结束***")) {
                    if (CoreEnv.runTime.isDebugMode) {
                        if (CoreEnv.runTime.isThrowException) {
                            CoreEnv.runTime.isThrowException=false
                            throw ScriptInterruptedException()
                        }
                    }
                    if (Thread.currentThread().isInterrupted) {
                        throw ScriptInterruptedException()
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            val isUiThread = Looper.myLooper() == Looper.getMainLooper()
            val timeUtils = TimeUtils
            val currentTimeStamp = timeUtils.getTime()
            val formattedTime = timeUtils.timeFormat(currentTimeStamp, "yyyy-MM-dd HH:mm:ss")
            val pid = Process.myPid()
            val strlog = "$formattedTime $pid $level $msg"
            CoreEnv.globalVM.get<Logcat>()?.log(strlog)
            if (!CoreEnv.runTime.isDebugMode) return
            if (CoreEnv.runTime.isRemoteMode){
                if (isUiThread) {
                    CoroutineScope(Dispatchers.IO).launch {
                        DebugController.send(strlog)
                    }
                } else {
                    DebugController.send(strlog)
                }
            }else{
                sleep(100)
                ViewLogger.logger?.addLog(strlog)
            }

        }

    }




    fun i(msg: String) {
        XLog.i(msg)
        logMessage("I", msg)
    }

    fun d(msg: String)  {
        XLog.d(msg)
        logMessage("D", msg)
    }

    fun w(msg: String)  {
        XLog.w(msg)
        logMessage("W", msg)
    }

    fun e(msg: String) {
        XLog.e(msg)
        logMessage("E", msg)
    }

    fun e(msg: String, tr: Throwable) {
        XLog.e(msg, tr)
        val errorMsg = "$msg\n${tr.stackTraceToString()}"
        logMessage("E", errorMsg)
    }


}