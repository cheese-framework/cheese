package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.api.WebSocket
import net.codeocean.cheese.core.utils.WebSocketCallback
import net.codeocean.cheese.core.utils.WebSocketUtils
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

object WebSocketImpl:WebSocket {
    override fun server(port: Int, callback: WebSocketCallback<ClientHandshake>) {
        WebSocketUtils.WebSocketServerWith(port, callback).start()
    }

    override fun client(uriString: String, callback: WebSocketCallback<ServerHandshake>) {

        WebSocketUtils.WebSocketClientWith(URI(uriString), callback).connect()
    }
}