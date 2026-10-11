package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.VideoRepository

class GetVideosUseCase(
    private val repository: VideoRepository,
) {
    suspend operator fun invoke(): Result<List<Video>> =
        runCatching { repository.searchKidsVideos(BABY_VIDEOS_QUERY) }

    private companion object {
        const val BABY_VIDEOS_QUERY = "nursery rhymes baby songs"
    }
}
