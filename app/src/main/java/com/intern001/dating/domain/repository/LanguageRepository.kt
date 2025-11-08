package com.intern001.dating.domain.repository

import com.intern001.dating.domain.model.Language

interface LanguageRepository {
    suspend fun getLanguages(): List<Language>

    suspend fun prefetchLanguages()
}
