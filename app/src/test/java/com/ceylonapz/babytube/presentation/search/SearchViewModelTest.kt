package com.ceylonapz.babytube.presentation.search

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository
import com.ceylonapz.babytube.domain.usecase.SearchVideosUseCase
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
class SearchViewModelTest {

    private val video = Video("1", "Baby Shark", "Pinkfong", "")
    private var results: (String) -> List<Video> = { listOf(video) }
    private val searched = mutableListOf<String>()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val repository = object : VideoRepository {
            override suspend fun getCategoryVideos(category: VideoCategory) = emptyList<Video>()
            override suspend fun searchKidsVideos(query: String, regionCode: String?): List<Video> {
                searched += query
                return results(query)
            }
        }
        val country = object : CountryRepository { override fun getCountryCode(): String? = null }
        viewModel = SearchViewModel(SearchVideosUseCase(repository, country))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts idle and does not search while typing`() {
        viewModel.onQueryChange("baby")

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
        assertTrue(searched.isEmpty())
    }

    @Test
    fun `submitting typed text shows results`() {
        viewModel.onQueryChange("baby shark")
        viewModel.search()

        assertEquals(SearchUiState.Results(listOf(video)), viewModel.uiState.value)
    }

    @Test
    fun `voice result fills the query and searches`() {
        viewModel.onVoiceResult("wheels on the bus")

        assertEquals("wheels on the bus", viewModel.query.value)
        assertEquals(listOf("wheels on the bus"), searched)
    }

    @Test
    fun `empty results show no-results state`() {
        results = { emptyList() }

        viewModel.onVoiceResult("zzz")

        assertEquals(SearchUiState.NoResults("zzz"), viewModel.uiState.value)
    }

    @Test
    fun `errors are shown`() {
        results = { error("quota exceeded") }

        viewModel.onVoiceResult("q")

        assertEquals(SearchUiState.Error("quota exceeded"), viewModel.uiState.value)
    }

    @Test
    fun `blank submit is ignored`() {
        viewModel.onQueryChange("   ")
        viewModel.search()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
        assertTrue(searched.isEmpty())
    }
}
