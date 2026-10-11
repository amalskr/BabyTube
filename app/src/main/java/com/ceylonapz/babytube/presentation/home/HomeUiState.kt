package com.ceylonapz.babytube.presentation.home

import com.ceylonapz.babytube.domain.Video

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val videos: List<Video>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
