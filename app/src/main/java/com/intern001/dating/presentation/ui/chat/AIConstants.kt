package com.intern001.dating.presentation.ui.chat

import com.intern001.dating.domain.model.MatchCard
import com.intern001.dating.domain.model.Photo
import com.intern001.dating.domain.model.UserLocation
import java.util.Date

object AIConstants {
    const val AI_ASSISTANT_USER_ID = "AI_ASSISTANT"
    const val AI_ASSISTANT_NAME = "David Vu"
    const val AI_TYPING_TIMEOUT_MS = 30_000L

    const val AI_FAKE_MATCH_ID = "AI_ASSISTANT_MATCH"
    const val AI_FAKE_NAME = "David Vu"
    const val AI_FAKE_AVATAR_URL = "https://i.pravatar.cc/300?img=68"
    const val AI_FAKE_AGE = 25
    const val AI_FAKE_CITY = "Hà Nội"

    const val AI_FAKE_BIO = "Xin chào! Tôi là David, một người thích khám phá và trải nghiệm cuộc sống. Tôi yêu thích du lịch, đọc sách và nấu ăn. Hãy cùng nhau chia sẻ những câu chuyện thú vị nhé! 😊"
    const val AI_FAKE_GENDER = "male"
    const val AI_FAKE_OCCUPATION = "Software Engineer"
    const val AI_FAKE_COMPANY = "Tech Company"
    const val AI_FAKE_EDUCATION = "Đại học Bách Khoa Hà Nội"
    const val AI_FAKE_HEIGHT = 175
    const val AI_FAKE_ZODIAC_SIGN = "Sư Tử"
    const val AI_FAKE_RELATIONSHIP_MODE = "serious"

    val AI_FAKE_INTERESTS = listOf(
        "Du lịch",
        "Đọc sách",
        "Nấu ăn",
        "Âm nhạc",
        "Thể thao",
        "Phim ảnh",
        "Công nghệ",
        "Nhiếp ảnh",
    )

    val AI_FAKE_PHOTOS = listOf(
        "https://i.pravatar.cc/800?img=68",
        "https://i.pravatar.cc/800?img=12",
        "https://i.pravatar.cc/800?img=33",
        "https://i.pravatar.cc/800?img=45",
    )

    fun isAIConversation(userId: String?): Boolean {
        return userId == AI_ASSISTANT_USER_ID
    }

    fun isMessageFromAI(senderId: String?): Boolean {
        return senderId == AI_ASSISTANT_USER_ID
    }

    /**
     * Kiểm tra xem userId có phải AI user không
     * Alias cho isAIConversation để phù hợp với hướng dẫn
     */
    fun isAIUser(userId: String?): Boolean {
        return userId == AI_ASSISTANT_USER_ID
    }

    fun createAIFakeProfile(): MatchCard {
        val now = Date()
        return MatchCard(
            id = AI_ASSISTANT_USER_ID,
            userId = AI_ASSISTANT_USER_ID,
            firstName = "David",
            lastName = "Vu",
            displayName = AI_FAKE_NAME,
            age = AI_FAKE_AGE,
            gender = AI_FAKE_GENDER,
            avatar = AI_FAKE_AVATAR_URL,
            photos = AI_FAKE_PHOTOS.mapIndexed { index, url ->
                Photo(
                    id = "ai_photo_$index",
                    userId = AI_ASSISTANT_USER_ID,
                    url = url,
                    cloudinaryPublicId = null,
                    type = if (index == 0) "avatar" else "gallery",
                    source = "url",
                    isPrimary = index == 0,
                    order = index,
                    isVerified = false,
                    width = null,
                    height = null,
                    fileSize = null,
                    format = null,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                )
            },
            bio = AI_FAKE_BIO,
            distance = null,
            location = UserLocation(
                latitude = 21.0285,
                longitude = 105.8542,
                city = AI_FAKE_CITY,
                country = "Vietnam",
            ),
            occupation = AI_FAKE_OCCUPATION,
            company = AI_FAKE_COMPANY,
            education = AI_FAKE_EDUCATION,
            interests = AI_FAKE_INTERESTS,
            relationshipMode = AI_FAKE_RELATIONSHIP_MODE,
            height = AI_FAKE_HEIGHT,
            zodiacSign = AI_FAKE_ZODIAC_SIGN,
            isVerified = false,
        )
    }
}
