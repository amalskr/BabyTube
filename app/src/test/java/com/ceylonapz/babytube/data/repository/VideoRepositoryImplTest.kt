package com.ceylonapz.babytube.data.repository

import com.ceylonapz.babytube.data.remote.VideoRemoteDataSource
import com.ceylonapz.babytube.data.remote.dto.SnippetDto
import com.ceylonapz.babytube.data.remote.dto.StatusDto
import com.ceylonapz.babytube.data.remote.dto.ThumbnailDto
import com.ceylonapz.babytube.data.remote.dto.ThumbnailsDto
import com.ceylonapz.babytube.data.remote.dto.VideoItemDto
import com.ceylonapz.babytube.domain.Video
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoRepositoryImplTest {

    private fun item(id: String, madeForKids: Boolean, embeddable: Boolean = true) = VideoItemDto(
        id = id,
        snippet = SnippetDto("Title $id", "Channel", ThumbnailsDto(medium = ThumbnailDto("https://img/$id"))),
        status = StatusDto(embeddable = embeddable, madeForKids = madeForKids),
    )

    @Test
    fun `keeps only embeddable made-for-kids videos`() = runTest {
        val remote = FakeRemote(
            listOf(item("kids", true), item("adult", false), item("locked", true, embeddable = false))
        )
        val repository = VideoRepositoryImpl(remote, UnconfinedTestDispatcher(testScheduler))

        val videos = repository.searchKidsVideos("baby songs")

        assertEquals(listOf(Video("kids", "Title kids", "Channel", "https://img/kids")), videos)
    }

    @Test
    fun `skips videos call when search is empty`() = runTest {
        val remote = FakeRemote(emptyList())
        val repository = VideoRepositoryImpl(remote, UnconfinedTestDispatcher(testScheduler))

        assertEquals(emptyList<Video>(), repository.searchKidsVideos("q"))
        assertEquals(0, remote.getVideosCalls)
    }

    private class FakeRemote(val items: List<VideoItemDto>) : VideoRemoteDataSource {
        var getVideosCalls = 0
        override suspend fun searchVideoIds(query: String, maxResults: Int) = items.map { it.id }
        override suspend fun getVideos(ids: List<String>): List<VideoItemDto> {
            getVideosCalls++
            return items
        }
    }
}
