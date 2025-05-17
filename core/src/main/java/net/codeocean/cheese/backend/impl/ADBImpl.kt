package net.codeocean.cheese.backend.impl

import android.util.Log
import net.codeocean.cheese.backend.impl.ADBImpl.SocketClient.Companion.runShell
import net.codeocean.cheese.core.api.ADB
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object ADBImpl:ADB {
    override fun exec(command: String): String {
      return  runShell(command)
    }
    class SocketClient(private val cmd: String, private val callback: OnServiceSend) {

        private val TAG = "SocketClient"
        private val HOST = "127.0.0.1"
        private val PORT = 4521

        private lateinit var printWriter: PrintWriter
        private lateinit var bufferedReader: BufferedReader

        init {
            try {
                Log.d(TAG, "与service进行socket通讯,地址=$HOST:$PORT")
                val socket = Socket()
                socket.connect(InetSocketAddress(HOST, PORT), 5000)
                socket.soTimeout = 3000
                Log.d(TAG, "与service进行socket通讯,超时为：5000")

                printWriter = PrintWriter(socket.getOutputStream(), true)
                bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                CreateServerThread(socket)
                send(cmd)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "与service进行socket通讯发生错误 $e")
                callback.getSend("###ShellRunError: ${e}")
            }
        }

        private inner class CreateServerThread(private val socket: Socket) : Thread() {
            init {
                Log.d(TAG, "创建了一个新的连接线程")
                start()
            }

            override fun run() {
                try {
                    val inputStreamReader = InputStreamReader(socket.getInputStream())
                    val reader = BufferedReader(inputStreamReader)
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let { callback.getSend(it) }
                    }
                    Log.d(TAG, "客户端接收解析服务器的while循环结束")
                    bufferedReader.close()
                    printWriter.close()
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "socket 接收线程发生错误：$e")
                }
            }
        }

        fun send(cmd: String) {
            printWriter.println(cmd)
            printWriter.flush()
        }

        interface OnServiceSend {
            fun getSend(result: String)
        }

        companion object {
            @JvmStatic
            fun runShell(cmd: String?): String {
                if (cmd.isNullOrEmpty()) return "命令为空"

                val resultFuture = CompletableFuture<String>()

                Thread {
                    SocketClient(cmd, object : OnServiceSend {
                        override fun getSend(result: String) {
                            resultFuture.complete(result)
                        }
                    })
                }.start()

                return try {
                    resultFuture.get(5, TimeUnit.SECONDS)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    "执行出错"
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    "执行出错"
                } catch (e: TimeoutException) {
                    e.printStackTrace()
                    "执行超时"
                }
            }
        }
    }
}