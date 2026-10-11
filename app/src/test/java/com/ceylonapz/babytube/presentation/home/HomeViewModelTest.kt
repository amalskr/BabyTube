package com.ceylonapz.babytube.presentation.home

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.VideoRepository
import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.domain.usecase.GetVideoRowsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `emits success with videos`() {
        val videos = listOf(Video("1", "Title", "Channel", ""))
        val viewModel = HomeViewModel(useCase(FakeRepository { videos }))

        assertEquals(HomeUiState.Success(listOf(VideoRow("Row", videos))), viewModel.uiState.value)
    }

    @Test
    fun `emits error when loading fails`() {
        val viewModel = HomeViewModel(useCase(FakeRepository { error("boom") }))

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    @Test
    fun `emits error when no videos are found`() {
        val viewModel = HomeViewModel(useCase(FakeRepository { emptyList() }))

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    private fun useCase(repository: VideoRepository) =
        GetVideoRowsUseCase(repository, listOf(VideoCategory("Row", "query")))

    private class FakeRepository(val block: () -> List<Video>) : VideoRepository {
        override suspend fun searchKidsVideos(query: String): List<Video> = block()
    }
}
