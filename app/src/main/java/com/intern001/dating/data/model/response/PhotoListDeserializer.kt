package com.intern001.dating.data.model.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer to handle photos field which can be:
 * 1. Array of strings: ["url1", "url2"]
 * 2. Array of objects: [{"url": "url1", "order": 0}, ...]
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
                    // Case 1: Element is a string URL
                    element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                        val url = element.asString
                        photos.add(
                            PhotoResponse(
                                url = url,
                                order = index,
                                uploadedAt = null,
                                id = null,
                            ),
                        )
                    }
                    // Case 2: Element is an object
                    element.isJsonObject -> {
                        val obj = element.asJsonObject
                        val url = obj.get("url")?.asString ?: ""
                        val order = obj.get("order")?.asInt ?: index
                        val uploadedAt = obj.get("uploadedAt")?.asString
                        val id = obj.get("id")?.asString

                        if (url.isNotEmpty()) {
                            photos.add(
                                PhotoResponse(
                                    url = url,
                                    order = order,
                                    uploadedAt = uploadedAt,
                                    id = id,
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
