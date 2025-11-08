package com.intern001.dating.presentation.ui.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intern001.dating.domain.model.Language
import com.intern001.dating.domain.usecase.auth.GetLanguagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SelectLanguageViewModel @Inject constructor(
    private val getLanguagesUseCase: GetLanguagesUseCase,
) : ViewModel() {

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>> get() = _languages

    private val _selectedLanguage = MutableLiveData<Language?>()
    val selectedLanguage: LiveData<Language?> get() = _selectedLanguage

    fun fetchLanguages() {
        viewModelScope.launch {
            _languages.value = getLanguagesUseCase()
        }
    }

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
    }
}
