package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.RecentSearchRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchVideosUseCaseTest {

    private val repository = FakeRepository()
    private val recent = FakeRecent()
    private val useCase = SearchVideosUseCase(repository, FakeCountry("LK"), recent)

    @Test
    fun `searches trimmed query in the viewer's region`() = runTest {
        useCase("  baby shark  ")

        assertEquals(listOf("baby shark" to "LK"), repository.searches)
    }

    @Test
    fun `blank query returns no videos without calling the api`() = runTest {
        assertEquals(emptyList<Video>(), useCase("   ").getOrThrow())
        assertTrue(repository.searches.isEmpty())
    }

    @Test
    fun `saves results as recent searches`() = runTest {
        useCase("baby shark")

        assertEquals(listOf(listOf(Video("1", "baby shark", "Channel", ""))), recent.saved)
    }

    @Test
    fun `does not save failed or blank searches`() = runTest {
        useCase(" ")
        repository.fail = true
        useCase("q")

        assertTrue(recent.saved.isEmpty())
    }

    @Test
    fun `wraps repository errors in failure`() = runTest {
        repository.fail = true

        assertTrue(useCase("q").isFailure)
    }

    private class FakeRepository : VideoRepository {
        val searches = mutableListOf<Pair<String, String?>>()
        var fail = false
        override suspend fun getCategoryVideos(category: VideoCategory): List<Video> = emptyList()
        override suspend fun searchKidsVideos(query: String, regionCode: String?): List<Video> {
            if (fail) error("boom")
            searches += query to regionCode
            return listOf(Video("1", query, "Channel", ""))
        }
    }

    private class FakeRecent : RecentSearchRepository {
        val saved = mutableListOf<List<Video>>()
        override fun observeRecentVideos(): Flow<List<Video>> = flowOf(emptyList())
        override suspend fun saveSearchResults(videos: List<Video>) {
            saved += videos
        }
    }

    private class FakeCountry(val code: String?) : CountryRepository {
        override fun getCountryCode() = code
    }
}
