package com.ceylonapz.babytube.presentation.home

import com.ceylonapz.babytube.domain.VideoRow

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val rows: List<VideoRow>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
