package com.intern001.dating.data.service

import android.util.Log
import com.intern001.dating.BuildConfig
import com.intern001.dating.data.model.MessageModel
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import org.json.JSONArray
import org.json.JSONObject

class ChatSocketService(private val token: String) {
    private var socket: Socket? = null
    private val TAG = "ChatSocketService"
    var onConnectionStatusChanged: ((Boolean) -> Unit)? = null
    private var chatHistoryListenerRegistered = false
    private var receiveMessageListenerRegistered = false

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
            // Note: once() automatically removes listener after first call, so no duplicate risk
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
        // Remove existing listener to prevent duplicates
        if (chatHistoryListenerRegistered) {
            socket?.off("chat_history")
        }
        socket?.on("chat_history") { args ->
            try {
                val messagesJson = args[0] as? JSONArray
                val messages = messagesJson?.let { parseMessages(it) } ?: emptyList()
                callback(messages)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing chat history", e)
            }
        }
        chatHistoryListenerRegistered = true
    }

    fun onReceiveMessage(callback: (MessageModel) -> Unit) {
        // Remove any existing listener first to avoid duplicates
        if (receiveMessageListenerRegistered) {
            socket?.off("receive_message")
        }

        socket?.on("receive_message") { args ->
            try {
                val messageJson = args[0] as? JSONObject
                val message = messageJson?.let { parseMessage(it) }
                if (message != null) {
                    callback(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message", e)
            }
        }
        receiveMessageListenerRegistered = true
    }

    fun sendMessage(
        matchId: String,
        senderId: String,
        message: String? = null,
        audioPath: String? = null,
        duration: Int? = null,
        imgChat: String? = null,
        clientMessageId: String? = null,
        replyToMessageId: String? = null,
        replyToClientMessageId: String? = null,
        replyToTimestamp: String? = null,
        replyPreview: String? = null,
        replySenderId: String? = null,
        replySenderName: String? = null,
        reaction: String? = null,
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
            clientMessageId?.let { put("clientMessageId", it) }
            replyToMessageId?.let { put("replyToMessageId", it) }
            replyToClientMessageId?.let { put("replyToClientMessageId", it) }
            replyToTimestamp?.let { put("replyToTimestamp", it) }
            replyPreview?.let { put("replyPreview", it) }
            replySenderId?.let { put("replySenderId", it) }
            replySenderName?.let { put("replySenderName", it) }
            // Always send reaction field if provided (even if empty string)
            if (reaction != null) {
                put("reaction", reaction)
            }
        }
        socket?.emit("send_message", data)
        Log.d(TAG, "Sent message: matchId=$matchId, senderId=$senderId, message=${message?.take(50)}..., reaction=$reaction, replyToMessageId=$replyToMessageId")
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        chatHistoryListenerRegistered = false
        receiveMessageListenerRegistered = false
        pendingJoinRoom = null
        Log.d(TAG, "Socket disconnected and cleaned up")
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    private fun parseMessage(json: JSONObject): MessageModel {
        val audioPathValue = json.optString("audioPath", null)
        val imgChatValue = json.optString("imgChat", null)

        // Parse reaction carefully
        val reactionValue = if (json.has("reaction")) {
            val r = json.optString("reaction", null)
            if (r.isNullOrBlank() || r == "null") null else r
        } else {
            null
        }

        val replyToMessageId = json.optString("replyToMessageId", null).takeIf { it?.isNotBlank() == true }

        return MessageModel(
            id = json.optString("_id", null).takeIf { it?.isNotBlank() == true }
                ?: json.optString("id", null).takeIf { it?.isNotBlank() == true },
            clientMessageId = json.optString("clientMessageId", null).takeIf { it?.isNotBlank() == true },
            senderId = json.optString("senderId"),
            matchId = json.optString("matchId"),
            message = json.optString("message", ""),
            timestamp = json.optString("timestamp", null),
            imgChat = if (imgChatValue.isNullOrBlank()) null else imgChatValue,
            audioPath = if (audioPathValue.isNullOrBlank()) null else audioPathValue,
            duration = if (json.has("duration") && json.optInt("duration") > 0) json.optInt("duration") else null,
            delivered = if (json.has("delivered")) json.optBoolean("delivered") else null,
            replyToMessageId = replyToMessageId,
            replyToClientMessageId = json.optString("replyToClientMessageId", null).takeIf { it?.isNotBlank() == true },
            replyToTimestamp = json.optString("replyToTimestamp", null).takeIf { it?.isNotBlank() == true },
            replyPreview = json.optString("replyPreview", null).takeIf { it?.isNotBlank() == true },
            replySenderId = json.optString("replySenderId", null).takeIf { it?.isNotBlank() == true },
            replySenderName = json.optString("replySenderName", null).takeIf { it?.isNotBlank() == true },
            reaction = reactionValue,
            isReactionMessage = if (json.has("isReactionMessage")) json.optBoolean("isReactionMessage") else null,
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
