package com.intern001.dating.data.repository

import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.data.model.response.UploadImageResponse
import com.intern001.dating.data.model.response.toEntity
import com.intern001.dating.domain.entity.LastMessageEntity
import com.intern001.dating.domain.repository.ChatRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ChatRepositoryImpl @Inject constructor(
    @Named("datingApi") private val api: DatingApiService,
) : ChatRepository {
    override suspend fun sendMessage(message: MessageModel): MessageModel = api.sendMessage(message)

    override suspend fun getHistory(matchId: String): List<MessageModel> = api.getHistory(matchId)

    override suspend fun getLastMessage(matchId: String): LastMessageEntity {
        val resp = api.getLastMessage(matchId)
        return resp.toEntity()
    }
    override suspend fun uploadAudio(localPath: String): String? {
        val file = File(localPath)
        val reqFile = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", file.name, reqFile)
        val uploadRes = api.uploadAudio(audioPart)
        if (uploadRes.isSuccessful) {
            return uploadRes.body()?.url
        }
        return null
    }
    override suspend fun uploadImage(file: MultipartBody.Part): UploadImageResponse? {
        val res = api.uploadChatImage(file)
        return if (res.isSuccessful) res.body() else null
    }
}
