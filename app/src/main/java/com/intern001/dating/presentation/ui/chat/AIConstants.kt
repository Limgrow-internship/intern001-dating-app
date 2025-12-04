package com.intern001.dating.presentation.ui.chat

/**
 * Constants and helper functions for AI Assistant integration
 */
object AIConstants {
    const val AI_ASSISTANT_USER_ID = "AI_ASSISTANT"
    const val AI_ASSISTANT_NAME = "AI Assistant"
    const val AI_TYPING_TIMEOUT_MS = 30_000L // 30 seconds

    /**
     * Check if a conversation is an AI conversation
     */
    fun isAIConversation(userId: String?): Boolean {
        return userId == AI_ASSISTANT_USER_ID
    }

    /**
     * Check if a message is from AI
     */
    fun isMessageFromAI(senderId: String?): Boolean {
        return senderId == AI_ASSISTANT_USER_ID
    }
}
