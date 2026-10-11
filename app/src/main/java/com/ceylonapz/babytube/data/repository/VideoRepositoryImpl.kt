package com.ceylonapz.babytube.data.repository

import com.ceylonapz.babytube.data.mapper.toDomain
import com.ceylonapz.babytube.data.remote.VideoRemoteDataSource
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VideoRepositoryImpl(
    private val remoteDataSource: VideoRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : VideoRepository {

    override suspend fun searchKidsVideos(query: String): List<Video> = withContext(ioDispatcher) {
        val ids = remoteDataSource.searchVideoIds(query, MAX_RESULTS)
        if (ids.isEmpty()) return@withContext emptyList()

        // search.list can't filter on madeForKids, so check each video's status.
        remoteDataSource.getVideos(ids)
            .filter { it.status.madeForKids && it.status.embeddable }
            .map { it.toDomain() }
    }

    private companion object {
        const val MAX_RESULTS = 25
    }
}
