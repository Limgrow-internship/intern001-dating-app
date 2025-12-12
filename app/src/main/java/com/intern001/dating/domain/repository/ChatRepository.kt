package com.intern001.dating.domain.repository

import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.model.response.MatchStatusResponse
import com.intern001.dating.data.model.response.UploadImageResponse
import com.intern001.dating.domain.entity.LastMessageEntity
import okhttp3.MultipartBody

interface ChatRepository {
    suspend fun sendMessage(message: MessageModel): MessageModel
    suspend fun getHistory(matchId: String): List<MessageModel>

    suspend fun getLastMessage(matchId: String): LastMessageEntity
    suspend fun uploadAudio(localPath: String): String?

    suspend fun uploadImage(file: MultipartBody.Part): UploadImageResponse?

    suspend fun deleteAllMessages(matchId: String)

    suspend fun unmatch(targetUserId: String)

    suspend fun getMatchStatus(matchId: String): String

    suspend fun getMatchStatusResponse(targetUserId: String): MatchStatusResponse

    suspend fun clearMessagesForUser(matchId: String)

    suspend fun block(targetUserId: String)

    suspend fun unblock(targetUserId: String)
}
