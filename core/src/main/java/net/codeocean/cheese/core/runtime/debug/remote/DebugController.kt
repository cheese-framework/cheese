package net.codeocean.cheese.core.runtime.debug.remote

import net.codeocean.cheese.core.Script
import org.java_websocket.client.WebSocketClient
import java.io.File
import java.lang.Thread.sleep
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object DebugController {
    private var client: WebSocketClient? = null


    fun createNamedThreadPool(): ExecutorService {
        return Executors.newCachedThreadPool { runnable ->
            Thread(runnable).apply {
                name = "MainRunThread-${System.nanoTime()}"
            }
        }
    }

    fun connect(
        script: Script,
        ip: String,
        onOpen: () -> Unit, onClose: () -> Unit, onError: (String) -> Unit
    ) {
        try {
            client = DebugClient(script,URI("ws://${ip.trim()}/chat"), onOpen, onClose, onError)
            client!!.connect()
            sleep(1000)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message.toString())
        }
    }

    fun sendFile(cmd: String, filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val cmdBytes = cmd.toByteArray()
            val fileName = file.name
            val fileBytes = file.readBytes()
            val fileNameBytes = fileName.toByteArray()
            val separator = "|".toByteArray()
            val combinedBuffer =
                ByteBuffer.allocate(cmdBytes.size + separator.size + fileNameBytes.size + separator.size + fileBytes.size)
            combinedBuffer.put(cmdBytes)
            combinedBuffer.put(separator)
            combinedBuffer.put(fileNameBytes)
            combinedBuffer.put(separator)
            combinedBuffer.put(fileBytes)
            combinedBuffer.flip()
            client?.send(combinedBuffer)


        } else {
            println("File not found: $filePath")
        }

    }

    fun send(text: String) {
        client?.send(text)
    }

    fun close() {
        client?.close()
    }

}