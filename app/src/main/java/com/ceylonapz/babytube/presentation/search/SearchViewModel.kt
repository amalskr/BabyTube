package com.ceylonapz.babytube.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceylonapz.babytube.domain.usecase.SearchVideosUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchVideos: SearchVideosUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _query.value = query
    }

    /** Spoken text replaces the query and searches straight away. */
    fun onVoiceResult(text: String) {
        _query.value = text
        search()
    }

    // Only runs on submit, not per keystroke: every search costs ~101 quota units.
    fun search() {
        val query = _query.value.trim()
        if (query.isEmpty()) return
        searchJob?.cancel()
        _uiState.value = SearchUiState.Loading
        searchJob = viewModelScope.launch {
            _uiState.value = searchVideos(query).fold(
                onSuccess = { if (it.isEmpty()) SearchUiState.NoResults(query) else SearchUiState.Results(it) },
                onFailure = { SearchUiState.Error(it.message ?: "Something went wrong") },
            )
        }
    }
}
