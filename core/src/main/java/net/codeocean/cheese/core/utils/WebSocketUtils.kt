package net.codeocean.cheese.core.utils


import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.URI


interface WebSocketCallback<T> {
    fun onOpen(conn: WebSocket?, handshake: T?)
    fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean)
    fun onMessage(conn: WebSocket?, message: String?)
    fun onError(conn: WebSocket?, ex: Exception?)
    fun onStart()
}


class WebSocketUtils {


    class WebSocketServerWith(
        port: Int,
        private val callback: WebSocketCallback<ClientHandshake>
    ) : WebSocketServer(InetSocketAddress(port)) {

        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            callback.onOpen(conn, handshake)
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            callback.onClose(conn, code, reason, remote)
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
            callback.onMessage(conn, message)
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            callback.onError(conn, ex)
        }

        override fun onStart() {
            callback.onStart()
        }
    }

    class WebSocketClientWith(
        serverUri: URI,
        private val callback: WebSocketCallback<ServerHandshake>
    ) : WebSocketClient(serverUri) {

        override fun onOpen(handshakedata: ServerHandshake?) {

            callback.onOpen(this, handshakedata)
        }

        override fun onMessage(message: String?) {
            callback.onMessage(this, message)
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            callback.onClose(this, code, reason, remote)
        }

        override fun onError(ex: Exception?) {
            callback.onError(this, ex)
        }
    }


}