package com.ceylonapz.babytube.data.repository

import com.ceylonapz.babytube.data.source.VideoDataSource
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VideoRepositoryImpl(
    private val dataSource: VideoDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : VideoRepository {
    override suspend fun getVideos(): List<Video> = withContext(ioDispatcher) {
        dataSource.fetchVideos()
    }

    override suspend fun getVideo(id: String): Video? =
        getVideos().firstOrNull { it.id == id }
}
