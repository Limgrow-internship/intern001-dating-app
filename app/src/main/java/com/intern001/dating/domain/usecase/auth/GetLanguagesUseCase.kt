package com.intern001.dating.domain.usecase.auth

import com.intern001.dating.domain.model.Language
import com.intern001.dating.domain.repository.LanguageRepository
import javax.inject.Inject

class GetLanguagesUseCase @Inject constructor(
    private val repo: LanguageRepository,
) {
    suspend operator fun invoke(): List<Language> = repo.getLanguages()
}
