package com.ceylonapz.babytube.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.domain.usecase.GetVideoRowsUseCase
import com.ceylonapz.babytube.domain.usecase.ObserveRecentSearchVideosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getVideoRows: GetVideoRowsUseCase,
    observeRecentSearchVideos: ObserveRecentSearchVideosUseCase,
) : ViewModel() {

    private val fetchedState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    // Recent searches come from local storage and update live (e.g. after returning from
    // search), without refetching the YouTube rows. The row always goes first.
    val uiState: StateFlow<HomeUiState> =
        combine(fetchedState, observeRecentSearchVideos()) { fetched, recent ->
            if (fetched is HomeUiState.Success && recent.isNotEmpty()) {
                HomeUiState.Success(listOf(VideoRow(RECENT_SEARCH_TITLE, recent)) + fetched.rows)
            } else {
                fetched
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState.Loading)

    init {
        loadVideos()
    }

    fun loadVideos() {
        fetchedState.value = HomeUiState.Loading
        viewModelScope.launch {
            fetchedState.value = getVideoRows().fold(
                onSuccess = { rows ->
                    if (rows.isEmpty()) HomeUiState.Error("No videos found") else HomeUiState.Success(rows)
                },
                onFailure = { HomeUiState.Error(it.message ?: "Something went wrong") },
            )
        }
    }

    companion object {
        const val RECENT_SEARCH_TITLE = "Recent Search"
    }
}
