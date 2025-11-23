package com.intern001.dating.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer to handle photos field.
 * Backend now returns array of photo objects with full metadata.
 * For backward compatibility, still handles string URLs as fallback.
 */
class PhotoListDeserializer : JsonDeserializer<List<PhotoResponse>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): List<PhotoResponse> {
        if (json == null || !json.isJsonArray) {
            return emptyList()
        }

        val jsonArray = json.asJsonArray
        val photos = mutableListOf<PhotoResponse>()

        jsonArray.forEachIndexed { index, element ->
            try {
                when {
                    // Case 1: Element is a string URL (backward compatibility)
                    element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                        val url = element.asString
                        photos.add(
                            PhotoResponse(
                                id = "legacy_$index",
                                url = url,
                                type = "gallery",
                                source = "upload",
                                isPrimary = index == 0,
                                order = index,
                            ),
                        )
                    }
                    // Case 2: Element is a photo object (new format)
                    element.isJsonObject -> {
                        val obj = element.asJsonObject
                        val url = obj.get("url")?.asString ?: ""

                        if (url.isNotEmpty()) {
                            // Get ID from either _id or id field (MongoDB may use _id)
                            val id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: "photo_${System.currentTimeMillis()}_$index"
                            photos.add(
                                PhotoResponse(
                                    id = id,
                                    idAlt = if (obj.get("_id") != null) id else null,
                                    userId = obj.get("userId")?.asString,
                                    url = url,
                                    cloudinaryPublicId = obj.get("cloudinaryPublicId")?.asString,
                                    type = obj.get("type")?.asString ?: "gallery",
                                    source = obj.get("source")?.asString ?: "upload",
                                    isPrimary = obj.get("isPrimary")?.asBoolean ?: false,
                                    order = obj.get("order")?.asInt ?: index,
                                    isVerified = obj.get("isVerified")?.asBoolean ?: false,
                                    width = obj.get("width")?.asInt,
                                    height = obj.get("height")?.asInt,
                                    fileSize = obj.get("fileSize")?.asInt,
                                    format = obj.get("format")?.asString,
                                    isActive = obj.get("isActive")?.asBoolean ?: true,
                                    createdAt = obj.get("createdAt")?.asString,
                                    updatedAt = obj.get("updatedAt")?.asString,
                                ),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip invalid elements
                e.printStackTrace()
            }
        }

        return photos
    }
}
