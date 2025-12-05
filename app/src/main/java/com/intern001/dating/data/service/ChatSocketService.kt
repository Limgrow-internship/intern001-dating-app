package com.intern001.dating.data.service

import android.util.Log
import com.intern001.dating.BuildConfig
import com.intern001.dating.data.model.MessageModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException

class ChatSocketService(private val token: String) {
    private var socket: Socket? = null
    private val TAG = "ChatSocketService"
    var onConnectionStatusChanged: ((Boolean) -> Unit)? = null

    fun connect() {
        try {
            val options = IO.Options().apply {
                forceNew = true
                transports = arrayOf("websocket", "polling")
                // extraHeaders expects Map<String, List<String>>
                extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
            }

            val baseUrl = BuildConfig.BASE_URL
            val socketUrl = "$baseUrl/chat"
            socket = IO.socket(socketUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected successfully")
                onConnectionStatusChanged?.invoke(true)
                
                // Join room if there's a pending join request
                pendingJoinRoom?.let { (matchId, userId) ->
                    val data = JSONObject().apply {
                        put("matchId", matchId)
                        put("userId", userId)
                    }
                    socket?.emit("join_room", data)
                    Log.d(TAG, "Joined room after connection: matchId=$matchId, userId=$userId")
                    pendingJoinRoom = null
                }
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                onConnectionStatusChanged?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args[0] as? Throwable
                Log.e(TAG, "Socket connection error", error)
                onConnectionStatusChanged?.invoke(false)
            }

            socket?.connect()
            Log.d(TAG, "Attempting to connect to socket: $socketUrl")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid socket URL", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting socket", e)
        }
    }

    private var pendingJoinRoom: Pair<String, String>? = null
    
    fun joinRoom(matchId: String, userId: String) {
        if (socket == null) {
            Log.e(TAG, "Socket is null, cannot join room")
            return
        }
        
        val joinRoomData = {
            val data = JSONObject().apply {
                put("matchId", matchId)
                put("userId", userId)
            }
            socket?.emit("join_room", data)
            Log.d(TAG, "Joined room: matchId=$matchId, userId=$userId")
        }
        
        if (socket!!.connected()) {
            // Socket already connected, join immediately
            joinRoomData()
        } else {
            // Socket not connected yet, store join request and wait for connection
            Log.w(TAG, "Socket not connected yet, will join room after connection")
            pendingJoinRoom = Pair(matchId, userId)
            
            // This will be handled by EVENT_CONNECT listener
            if (pendingJoinRoom != null) {
                socket?.once(Socket.EVENT_CONNECT) {
                    pendingJoinRoom?.let { (mId, uId) ->
                        val data = JSONObject().apply {
                            put("matchId", mId)
                            put("userId", uId)
                        }
                        socket?.emit("join_room", data)
                        Log.d(TAG, "Joined room after connection: matchId=$mId, userId=$uId")
                        pendingJoinRoom = null
                    }
                }
            }
        }
    }

    fun onChatHistory(callback: (List<MessageModel>) -> Unit) {
        socket?.on("chat_history") { args ->
            try {
                val messagesJson = args[0] as? JSONArray
                val messages = messagesJson?.let { parseMessages(it) } ?: emptyList()
                callback(messages)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing chat history", e)
            }
        }
    }

    fun onReceiveMessage(callback: (MessageModel) -> Unit) {
        socket?.on("receive_message") { args ->
            try {
                val messageJson = args[0] as? JSONObject
                val message = messageJson?.let { parseMessage(it) }
                if (message != null) {
                    Log.d(TAG, "Received message: senderId=${message.senderId}, matchId=${message.matchId}, message=${message.message?.take(50)}...")
                    callback(message)
                } else {
                    Log.w(TAG, "Received message but parsing returned null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message", e)
            }
        }
        Log.d(TAG, "Registered receive_message listener")
    }

    fun sendMessage(
        matchId: String,
        senderId: String,
        message: String? = null,
        audioPath: String? = null,
        duration: Int? = null,
        imgChat: String? = null,
    ) {
        if (socket == null) {
            Log.e(TAG, "Socket is null, cannot send message")
            return
        }
        
        if (!socket!!.connected()) {
            Log.e(TAG, "Socket not connected, cannot send message")
            return
        }
        
        val data = JSONObject().apply {
            put("matchId", matchId)
            put("senderId", senderId)
            message?.let { put("message", it) } ?: put("message", "")
            audioPath?.let { put("audioPath", it) } ?: put("audioPath", "")
            imgChat?.let { put("imgChat", it) } ?: put("imgChat", "")
            duration?.let { put("duration", it) } ?: put("duration", 0)
        }
        socket?.emit("send_message", data)
        Log.d(TAG, "Sent message: matchId=$matchId, senderId=$senderId, message=${message?.take(50)}...")
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d(TAG, "Socket disconnected and cleaned up")
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    private fun parseMessage(json: JSONObject): MessageModel {
        val audioPathValue = json.optString("audioPath", null)
        val imgChatValue = json.optString("imgChat", null)

        return MessageModel(
            senderId = json.optString("senderId"),
            matchId = json.optString("matchId"),
            message = json.optString("message", ""),
            timestamp = json.optString("timestamp", null),
            imgChat = if (imgChatValue.isNullOrBlank()) null else imgChatValue,
            audioPath = if (audioPathValue.isNullOrBlank()) null else audioPathValue,
            duration = if (json.has("duration") && json.optInt("duration") > 0) json.optInt("duration") else null,
            delivered = if (json.has("delivered")) json.optBoolean("delivered") else null,
        )
    }

    private fun parseMessages(jsonArray: JSONArray): List<MessageModel> {
        val messages = mutableListOf<MessageModel>()
        for (i in 0 until jsonArray.length()) {
            try {
                val json = jsonArray.getJSONObject(i)
                messages.add(parseMessage(json))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message at index $i", e)
            }
        }
        return messages
    }
}

