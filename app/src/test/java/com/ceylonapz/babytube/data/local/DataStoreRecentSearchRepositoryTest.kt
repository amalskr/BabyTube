package com.ceylonapz.babytube.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ceylonapz.babytube.domain.Video
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreRecentSearchRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository() = DataStoreRecentSearchRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { tmp.newFile("recent.preferences_pb") }
    )

    private fun video(id: String) = Video(id, "Title $id", "Channel", "https://img/$id")

    @Test
    fun `starts empty`() = runTest {
        assertEquals(emptyList<Video>(), repository().observeRecentVideos().first())
    }

    @Test
    fun `newest search comes first without duplicates`() = runTest {
        val repository = repository()

        repository.saveSearchResults(listOf(video("a"), video("b")))
        repository.saveSearchResults(listOf(video("c"), video("a")))

        assertEquals(listOf("c", "a", "b"), repository.observeRecentVideos().first().map { it.id })
    }

    @Test
    fun `keeps at most 20 videos and round-trips every field`() = runTest {
        val repository = repository()

        repository.saveSearchResults((1..30).map { video("$it") })

        val stored = repository.observeRecentVideos().first()
        assertEquals(20, stored.size)
        assertEquals(video("1"), stored.first())
    }
}
