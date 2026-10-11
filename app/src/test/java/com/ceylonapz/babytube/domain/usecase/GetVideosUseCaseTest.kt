package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetVideosUseCaseTest {

    private val video = Video("1", "Title", "Channel", "", "")

    @Test
    fun `returns videos from repository`() = runTest {
        val useCase = GetVideosUseCase(FakeRepository { listOf(video) })

        assertEquals(listOf(video), useCase().getOrThrow())
    }

    @Test
    fun `wraps repository errors in failure`() = runTest {
        val useCase = GetVideosUseCase(FakeRepository { error("boom") })

        assertTrue(useCase().isFailure)
    }

    private class FakeRepository(val block: () -> List<Video>) : VideoRepository {
        override suspend fun getVideos(): List<Video> = block()
        override suspend fun getVideo(id: String): Video? = block().firstOrNull { it.id == id }
    }
}
