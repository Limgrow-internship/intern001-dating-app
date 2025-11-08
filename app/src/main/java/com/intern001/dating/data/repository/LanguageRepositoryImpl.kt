package com.intern001.dating.data.repository

import android.content.Context
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.domain.model.Language
import com.intern001.dating.domain.repository.LanguageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import org.json.JSONArray

class LanguageRepositoryImpl @Inject constructor(
    @Named("countryApi") private val api: DatingApiService,
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
            return it
        }

        val supportedCodes = loadSupportedLangCodes()

        val countries = api.getAllCountries()

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
        val result = langMap.map { Language(it.key, it.value) }.sortedBy { it.name }

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
