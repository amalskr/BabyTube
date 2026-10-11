package com.ceylonapz.babytube.presentation.home

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.RecentSearchRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository
import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.domain.usecase.GetVideoRowsUseCase
import com.ceylonapz.babytube.domain.usecase.ObserveRecentSearchVideosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val viewModel = HomeViewModel(useCase(FakeRepository { videos }), observeRecent())

        assertEquals(HomeUiState.Success(listOf(VideoRow("Row", videos))), viewModel.uiState.value)
    }

    @Test
    fun `emits error when loading fails`() {
        val viewModel = HomeViewModel(useCase(FakeRepository { error("boom") }), observeRecent())

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    @Test
    fun `emits error when no videos are found`() {
        val viewModel = HomeViewModel(useCase(FakeRepository { emptyList() }), observeRecent())

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    @Test
    fun `recent search row is always first and updates live`() {
        val fetched = Video("1", "Title", "Channel", "")
        val searched = Video("2", "Baby Shark", "Pinkfong", "")
        val viewModel = HomeViewModel(useCase(FakeRepository { listOf(fetched) }), observeRecent())

        assertEquals(listOf("Row"), (viewModel.uiState.value as HomeUiState.Success).rows.map { it.title })

        recentVideos.value = listOf(searched)

        assertEquals(
            listOf(VideoRow(HomeViewModel.RECENT_SEARCH_TITLE, listOf(searched)), VideoRow("Row", listOf(fetched))),
            (viewModel.uiState.value as HomeUiState.Success).rows,
        )
    }

    @Test
    fun `recent searches do not hide an error`() {
        recentVideos.value = listOf(Video("2", "Baby Shark", "Pinkfong", ""))

        val viewModel = HomeViewModel(useCase(FakeRepository { error("boom") }), observeRecent())

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }

    private val recentVideos = MutableStateFlow(emptyList<Video>())

    private fun observeRecent() = ObserveRecentSearchVideosUseCase(object : RecentSearchRepository {
        override fun observeRecentVideos(): Flow<List<Video>> = recentVideos
        override suspend fun saveSearchResults(videos: List<Video>) = Unit
    })

    private fun useCase(repository: VideoRepository) =
        GetVideoRowsUseCase(
            repository,
            countryRepository = object : CountryRepository { override fun getCountryCode(): String? = null },
            categories = listOf(VideoCategory("Row", "query")),
        )

    private class FakeRepository(val block: () -> List<Video>) : VideoRepository {
        override suspend fun getCategoryVideos(category: VideoCategory): List<Video> = block()

        override suspend fun searchKidsVideos(query: String, regionCode: String?): List<Video> = emptyList()
    }
}
