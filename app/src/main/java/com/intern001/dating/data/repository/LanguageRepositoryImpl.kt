package com.intern001.dating.data.repository

import android.content.Context
import com.intern001.dating.data.api.CountryApi
import com.intern001.dating.domain.model.Language
import com.intern001.dating.domain.repository.LanguageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.json.JSONArray

class LanguageRepositoryImpl @Inject constructor(
    private val api: CountryApi,
    @ApplicationContext private val context: Context,
) : LanguageRepository {

    private var cachedLanguages: List<Language>? = null

    private fun loadSupportedLangCodes(): List<String> {
        try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier("supported_languages", "raw", context.packageName),
            )
            val json = inputStream.bufferedReader().readText()
            val arr = JSONArray(json)
            return List(arr.length()) { arr.getString(it).lowercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun getLanguages(): List<Language> {
        cachedLanguages?.let {
            android.util.Log.d("LanguageRepo", "Return cachedLanguages: size=${it.size}")
            return it
        }

        val supportedCodes = loadSupportedLangCodes() // ["en","vi","ko" ...]
        android.util.Log.d("LanguageRepo", "Supported codes: $supportedCodes")

        val countries = api.getAllCountries() // lấy từ v2
        android.util.Log.d("LanguageRepo", "Countries fetch: size=${countries.size}")

        val langMap = mutableMapOf<String, String>()
        countries.forEach { country ->
            country.languages?.forEach { lang ->
                val code = lang.iso639_1?.lowercase() ?: return@forEach
                val name = lang.name ?: ""
                if (supportedCodes.contains(code)) {
                    langMap[code] = name
                }
            }
        }
        android.util.Log.d("LanguageRepo", "Filtered langMap: $langMap")

        val result = langMap.map { Language(it.key, it.value) }.sortedBy { it.name }
        android.util.Log.d("LanguageRepo", "Result languages: $result")

        cachedLanguages = result
        return result
    }

    override suspend fun prefetchLanguages() {
        if (cachedLanguages == null) {
            cachedLanguages = getLanguages()
        }
    }

    fun clearCache() {
        cachedLanguages = null
    }
}
