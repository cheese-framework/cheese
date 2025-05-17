package net.codeocean.cheese.core.api

import net.codeocean.cheese.core.utils.WebSocketCallback
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake

interface WebSocket {
    fun server(port: Int, callback: WebSocketCallback<ClientHandshake>)
    fun client(uriString: String, callback: WebSocketCallback<ServerHandshake>)
}