package net.codeocean.cheese.core.runtime.debug.remote

import com.elvishew.xlog.XLog
import com.hjq.toast.Toaster
import net.codeocean.cheese.backend.impl.PermissionsImpl.ACCESSIBILITY
import net.codeocean.cheese.core.CoreEnv
import net.codeocean.cheese.core.CoreFactory
import net.codeocean.cheese.core.Script
import net.codeocean.cheese.core.runtime.ScriptExecutionController
import net.codeocean.cheese.core.runtime.ScriptExecutionController.cancelAndClean
import net.codeocean.cheese.core.runtime.debug.remote.DebugController.sendFile
import net.codeocean.cheese.core.utils.FilesUtils

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.File
import java.net.URI
import java.util.concurrent.Executors

class DebugClient(
    private val script: Script,
    serverUri: URI,
    private val onOpen: () -> Unit,
    private val onClose: () -> Unit,
    private val onError: (String) -> Unit
) : WebSocketClient(serverUri) {

    enum class RunMode(val code: String) {
        RUN("1"),
        UI("2"),
        EXIT("3")
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        send("device")
        onOpen()
    }

    override fun onMessage(message: String) {
        System.gc()
        when (message) {
            RunMode.RUN.code -> {
                CoreEnv.runTime.isRemoteMode=true
                if (CoreEnv.executorMap.cancelAndClean("run") && ScriptExecutionController.downloadAndExtractDebugZip()) {

                    val executor = DebugController.createNamedThreadPool()

                    CoreEnv.executorMap["run"] = executor.submit {

                        XLog.i("运行命令")

                        script.run()


                    }

                }
            }

            RunMode.UI.code -> {
                if (CoreEnv.executorMap.cancelAndClean("run") && ScriptExecutionController.downloadAndExtractDebugZip()) {
                    CoreEnv.executorMap["run"] = Executors.newCachedThreadPool().submit {
                        XLog.i("运行UI命令")
                        CoreFactory.getWebView().runWebView("Keep")
                    }

                }

            }

            RunMode.EXIT.code ->  {
                XLog.i("停止命令")
                script.exit()
            }

            "4" -> {

                Thread {
                    try {
                        val workingDirectory = CoreFactory.getPath().WORKING_DIRECTORY // 获取工作目录，避免多次调用
                        val filePath = "$workingDirectory/work/paint.png"
                        val file = File(filePath)
                        file.delete()
                        if (CoreFactory.getRecordScreen().requestPermission(3)) {
                            CoreFactory.getRecordScreen().captureScreen(3, 0, 0, 0, -1)
                                ?.let { screenshot ->

                                    if (FilesUtils.save(screenshot, filePath)) {

                                        if (file.isFile) {
                                            // 发送文件
                                            sendFile("1", filePath)
                                            // 删除文件
                                        } else {
                                            Toaster.show("截图失败")
                                        }
                                    } else {
                                        Toaster.show("截图失败")
                                    }

                                }
                        } else {
                            // 如果没有权限，提示用户
                            Toaster.show("截图失败")
                        }
                    } catch (e: Exception) {
                        Toaster.show(e.message)
                    }

                }.start()

            }

            "5" -> {
                Thread {
                    try {
                        val workingDirectory = CoreFactory.getPath().WORKING_DIRECTORY // 缓存工作目录
                        val uixDir = "$workingDirectory/uix/"
                        val pngFile = File("$uixDir/uix.png")
                        val xmlFile = File("$uixDir/uix.xml")

                        // 删除旧文件
                        pngFile.delete()
                        xmlFile.delete()

                        // 检查截图权限
                        if (!CoreFactory.getRecordScreen().requestPermission(3)) {
                            Toaster.show("截图失败")
                            return@Thread
                        }

                        // 检查无障碍权限
                        if (!CoreFactory.getPermissions().requestPermission(ACCESSIBILITY, 3)) {
                            Toaster.show("获取节点失败")
                            return@Thread
                        }

                        // 截图并处理
                        CoreFactory.getRecordScreen().captureScreen(3, 0, 0, 0, -1)?.let { screenshot ->

                            if (  CoreFactory.getUiNode().getUi(uixDir)) {
                                if (FilesUtils.save(screenshot, pngFile.path)) {
                                    // 检查文件是否存在
                                    if (pngFile.isFile && xmlFile.isFile) {
                                        sendFile("2", pngFile.path)
                                        sendFile("3", xmlFile.path)
                                        // 删除文件
                                        pngFile.delete()
                                        xmlFile.delete()
                                    } else {
                                        Toaster.show("获取节点失败")
                                    }
                                } else {
                                    Toaster.show("获取节点失败")
                                }

                            } else {
                                Toaster.show("获取节点失败")
                            }

                        }
                    } catch (e: Exception) {
                        Toaster.show(e.message)
                    }

                }.start()

            }

            else -> println("x is something else")
        }


        XLog.i("Message from server: $message")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        XLog.i("Connection closed. Reason: $reason")
        onClose()
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
        XLog.e(ex)
        Toaster.show(ex)
//        onError(ex.message.toString())
    }
}
