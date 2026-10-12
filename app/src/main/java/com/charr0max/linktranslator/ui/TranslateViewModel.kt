package com.charr0max.linktranslator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charr0max.linktranslator.SpotifyResolver
import com.charr0max.linktranslator.Outcome
import com.charr0max.linktranslator.Option
import com.charr0max.linktranslator.SourceLink
import com.charr0max.linktranslator.WebTrackFinder
import com.charr0max.linktranslator.installedPlatforms
import com.charr0max.linktranslator.parseLink
import com.charr0max.linktranslator.preferredPlatform
import com.charr0max.linktranslator.setPreferredPlatform
import com.charr0max.linktranslator.refine
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
    private val resolver = SpotifyResolver()
    private val finder = WebTrackFinder()
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

    fun pick(choice: Outcome.Choose, option: Option, always: Boolean) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            if (always) getApplication<Application>().setPreferredPlatform(option.platform)
            _state.value = UiState.Launch(Outcome.Open(finder.refine(option, choice.track), choice.track))
        }
    }

    private fun run(link: SourceLink) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val app = getApplication<Application>()
            _state.value = try {
                when (val outcome = translate(link, resolver, app.installedPlatforms(), app.preferredPlatform())) {
                    is Outcome.Choose -> UiState.Picker(outcome)
                    is Outcome.Open -> UiState.Launch(outcome.copy(option = finder.refine(outcome.option, outcome.track)))
                    is Outcome.Browser -> UiState.Launch(outcome)
                }
            } catch (_: Exception) {
                UiState.Error(link)
            }
        }
    }
}
