package com.ceylonapz.babytube.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceylonapz.babytube.domain.usecase.GetVideoRowsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getVideoRows: GetVideoRowsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            _uiState.value = getVideoRows().fold(
                onSuccess = { rows ->
                    if (rows.isEmpty()) HomeUiState.Error("No videos found") else HomeUiState.Success(rows)
                },
                onFailure = { HomeUiState.Error(it.message ?: "Something went wrong") },
            )
        }
    }
}
