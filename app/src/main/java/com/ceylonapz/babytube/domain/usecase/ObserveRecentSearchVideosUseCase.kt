package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecentSearchVideosUseCase(
    private val repository: RecentSearchRepository,
) {
    operator fun invoke(): Flow<List<Video>> = repository.observeRecentVideos()
}
