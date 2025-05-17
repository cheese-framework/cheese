package net.codeocean.cheese.backend.impl

//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.ServiceConnection
//import android.os.*
//import android.util.Log
//import net.codeocean.cheese.termux.ITermuxTerminal
//import net.codeocean.cheese.termux.ITermuxTerminalCallback
//
//class TermuxAidlTerminal private constructor(
//    private val context: Context,
//    private val outputHandler: (String, Boolean) -> Unit
//) : ServiceConnection {
//
//    companion object {
//        fun create(
//            context: Context,
//            outputHandler: (String, Boolean) -> Unit,
//            onReady: (terminal: TermuxAidlTerminal) -> Unit
//        ): TermuxAidlTerminal {
//            return TermuxAidlTerminal(context, outputHandler).apply {
//                this.onReadyCallback = onReady
//                connect()
//            }
//        }
//    }
//
//    // 状态回调
//    private var onReadyCallback: ((TermuxAidlTerminal) -> Unit)? = null
//    private var onDisconnectCallback: (() -> Unit)? = null
//
//    // 服务控制
//    private var terminalService: ITermuxTerminal? = null
//    private var currentSessionId = -1
//    private val handler = Handler(Looper.getMainLooper())
//
//    // 线程安全的输出回调
//    private val callback = object : ITermuxTerminalCallback.Stub() {
//        override fun onOutput(output: String, isError: Boolean) {
//            handler.post { outputHandler(output, isError) }
//        }
//    }
//
//    fun connect() {
//        try {
//            Intent().apply {
//                setClassName("com.termux", "net.codeocean.cheese.termux.TermuxTerminalService")
//                action = "com.termux.app.ITermuxTerminal"
//            }.also { intent ->
//                if (!context.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
//                    outputHandler("Binding service failed", true)
//                }
//            }
//        } catch (e: Exception) {
//            outputHandler("Connection error: ${e.message}", true)
//        }
//    }
//
//    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//        try {
//            terminalService = ITermuxTerminal.Stub.asInterface(service).also { service ->
//                currentSessionId = service.createSession("/bin/bash", emptyArray()).also { id ->
//                    service.registerCallback(id, callback)
//                    handler.post {
//                        onReadyCallback?.invoke(this)
//                        outputHandler("Terminal session $id ready", false)
//                    }
//                }
//            }
//        } catch (e: RemoteException) {
//            handler.post {
//                outputHandler("Service initialization failed: ${e.message}", true)
//                safeUnbind()
//            }
//        }
//    }
//
//    fun sendInput(input: String) {
//        terminalService?.let { service ->
//            try {
//                service.writeInput(currentSessionId, "$input\n")
//            } catch (e: RemoteException) {
//                outputHandler("Input failed: ${e.message}", true)
//            }
//        } ?: outputHandler("Terminal not connected", true)
//    }
//
//    fun disconnect() {
//        try {
//            terminalService?.let {
//                it.unregisterCallback(currentSessionId)
//                it.closeSession(currentSessionId)
//            }
//        } catch (e: RemoteException) {
//            Log.w("TermuxAidlTerminal", "Cleanup error", e)
//        } finally {
//            safeUnbind()
//            handler.post { onDisconnectCallback?.invoke() }
//        }
//    }
//
//    private fun safeUnbind() {
//        try {
//            context.unbindService(this)
//        } catch (e: IllegalArgumentException) {
//            Log.w("TermuxAidlTerminal", "Already unbound", e)
//        }
//        terminalService = null
//        currentSessionId = -1
//    }
//
//    override fun onServiceDisconnected(name: ComponentName?) {
//        handler.post {
//            outputHandler("Service disconnected unexpectedly", true)
//            onDisconnectCallback?.invoke()
//        }
//    }
//
//    // Builder模式扩展
//    fun setOnDisconnectListener(callback: () -> Unit): TermuxAidlTerminal {
//        this.onDisconnectCallback = callback
//        return this
//    }
//}