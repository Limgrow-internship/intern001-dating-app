package com.intern001.dating.domain.usecase

import com.intern001.dating.data.model.MessageModel
import com.intern001.dating.domain.repository.ChatRepository
import javax.inject.Inject

class SendVoiceMessageUseCase @Inject constructor(private val repository: ChatRepository) {
    suspend operator fun invoke(
        matchId: String,
        senderId: String,
        localAudioPath: String,
        duration: Int,
        clientMessageId: String,
    ): MessageModel? {
        val audioUrl = repository.uploadAudio(localAudioPath) ?: return null
        val message = MessageModel(
            clientMessageId = clientMessageId,
            senderId = senderId,
            matchId = matchId,
            message = "",
            audioPath = audioUrl,
            duration = duration,
        )
        return repository.sendMessage(message)
    }
}
