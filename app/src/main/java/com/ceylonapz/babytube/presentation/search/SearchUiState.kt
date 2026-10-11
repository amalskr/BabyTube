package com.ceylonapz.babytube.presentation.search

import com.ceylonapz.babytube.domain.Video

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val videos: List<Video>) : SearchUiState
    data class NoResults(val query: String) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
