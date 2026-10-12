package com.charr0max.linktranslator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charr0max.linktranslator.OdesliResolver
import com.charr0max.linktranslator.Outcome
import com.charr0max.linktranslator.Platform
import com.charr0max.linktranslator.SourceLink
import com.charr0max.linktranslator.installedPlatforms
import com.charr0max.linktranslator.parseLink
import com.charr0max.linktranslator.preferredPlatform
import com.charr0max.linktranslator.setPreferredPlatform
import com.charr0max.linktranslator.translate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Loading : UiState
    data object NoLink : UiState
    data class Picker(val outcome: Outcome.Choose) : UiState
    data class Error(val source: SourceLink) : UiState
    data class Launch(val outcome: Outcome) : UiState
}

class TranslateViewModel(app: Application) : AndroidViewModel(app) {
    private val resolver = OdesliResolver()
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    private var source: SourceLink? = null

    fun start(text: String?) {
        if (source != null) return
        val link = text?.let(::parseLink)
        if (link == null) {
            _state.value = UiState.NoLink
            return
        }
        source = link
        run(link)
    }

    fun retry() {
        source?.let(::run)
    }

    fun remember(platform: Platform) {
        viewModelScope.launch { getApplication<Application>().setPreferredPlatform(platform) }
    }

    private fun run(link: SourceLink) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val app = getApplication<Application>()
            _state.value = try {
                when (val outcome = translate(link, resolver, app.installedPlatforms(), app.preferredPlatform())) {
                    is Outcome.Choose -> UiState.Picker(outcome)
                    else -> UiState.Launch(outcome)
                }
            } catch (_: Exception) {
                UiState.Error(link)
            }
        }
    }
}
