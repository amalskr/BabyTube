package com.ceylonapz.babytube.data.repository

import com.ceylonapz.babytube.data.local.SearchCursor
import com.ceylonapz.babytube.data.local.SearchCursorStore
import com.ceylonapz.babytube.data.remote.SearchPage
import com.ceylonapz.babytube.data.remote.VideoRemoteDataSource
import com.ceylonapz.babytube.data.remote.YouTubeApiException
import com.ceylonapz.babytube.data.remote.dto.SnippetDto
import com.ceylonapz.babytube.data.remote.dto.StatusDto
import com.ceylonapz.babytube.data.remote.dto.ThumbnailDto
import com.ceylonapz.babytube.data.remote.dto.ThumbnailsDto
import com.ceylonapz.babytube.data.remote.dto.VideoItemDto
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VideoRepositoryImplTest {

    private val category = VideoCategory("Sinhala", "සිංහල ළමා ගීත", languageCode = "si", regionCode = "LK")
    private val remote = FakeRemote()
    private val store = FakeCursorStore()

    private fun TestScope.repository() =
        VideoRepositoryImpl(remote, store, UnconfinedTestDispatcher(testScheduler))

    @Test
    fun `keeps only embeddable made-for-kids videos`() = runTest {
        remote.items = listOf(item("kids", true), item("adult", false), item("locked", true, embeddable = false))

        val videos = repository().getCategoryVideos(category)

        assertEquals(listOf(Video("kids", "Title kids", "Channel", "https://img/kids")), videos)
    }

    @Test
    fun `passes language and region to the search`() = runTest {
        repository().getCategoryVideos(category)

        assertEquals("si" to "LK", remote.searches.single().let { it.languageCode to it.regionCode })
    }

    @Test
    fun `each call continues from the saved page token`() = runTest {
        val repository = repository()
        remote.nextTokens = listOf("p2", "p3")

        repository.getCategoryVideos(category)
        repository.getCategoryVideos(category)

        assertEquals(listOf(null, "p2"), remote.searches.map { it.pageToken })
        assertEquals("p3", store.cursors.values.single()?.pageToken)
    }

    @Test
    fun `wraps back to the first page after the page limit`() = runTest {
        store.cursors[KEY] = SearchCursor("p5", pageIndex = 4)
        remote.nextTokens = listOf("p6")

        repository().getCategoryVideos(category)

        assertNull(store.cursors[KEY])
    }

    @Test
    fun `restarts from the first page when the saved token is invalid`() = runTest {
        store.cursors[KEY] = SearchCursor("expired", pageIndex = 2)
        remote.failToken = "expired"

        val videos = repository().getCategoryVideos(category)

        assertEquals(listOf("expired", null), remote.searches.map { it.pageToken })
        assertEquals(1, videos.size)
    }

    @Test
    fun `search returns kids videos from the first page without touching saved cursors`() = runTest {
        store.cursors[KEY] = SearchCursor("p3", pageIndex = 2)
        remote.items = listOf(item("kids", true), item("adult", false))
        remote.nextTokens = listOf("p2")

        val videos = repository().searchKidsVideos("baby shark", regionCode = "LK")

        assertEquals(listOf("kids"), videos.map { it.id })
        assertEquals(Search(pageToken = null, languageCode = null, regionCode = "LK"), remote.searches.single())
        assertEquals(SearchCursor("p3", pageIndex = 2), store.cursors[KEY])
    }

    @Test
    fun `skips videos call when search is empty`() = runTest {
        remote.items = emptyList()

        assertEquals(emptyList<Video>(), repository().getCategoryVideos(category))
        assertEquals(0, remote.getVideosCalls)
    }

    private fun item(id: String, madeForKids: Boolean, embeddable: Boolean = true) = VideoItemDto(
        id = id,
        snippet = SnippetDto("Title $id", "Channel", ThumbnailsDto(medium = ThumbnailDto("https://img/$id"))),
        status = StatusDto(embeddable = embeddable, madeForKids = madeForKids),
    )

    private data class Search(val pageToken: String?, val languageCode: String?, val regionCode: String?)

    private inner class FakeRemote : VideoRemoteDataSource {
        var items = listOf(item("kids", true))
        var nextTokens = emptyList<String>()
        var failToken: String? = null
        val searches = mutableListOf<Search>()
        var getVideosCalls = 0

        override suspend fun searchVideos(
            query: String,
            maxResults: Int,
            pageToken: String?,
            languageCode: String?,
            regionCode: String?,
        ): SearchPage {
            searches += Search(pageToken, languageCode, regionCode)
            if (pageToken != null && pageToken == failToken) {
                throw YouTubeApiException("bad token", reason = "invalidPageToken")
            }
            return SearchPage(items.map { it.id }, nextTokens.getOrNull(searches.size - 1))
        }

        override suspend fun getVideos(ids: List<String>): List<VideoItemDto> {
            getVideosCalls++
            return items
        }
    }

    private class FakeCursorStore : SearchCursorStore {
        val cursors = mutableMapOf<String, SearchCursor?>()
        override suspend fun get(key: String) = cursors[key]
        override suspend fun set(key: String, cursor: SearchCursor?) {
            if (cursor == null) cursors.remove(key) else cursors[key] = cursor
        }
    }

    private companion object {
        const val KEY = "සිංහල ළමා ගීත|si|LK"
    }
}
