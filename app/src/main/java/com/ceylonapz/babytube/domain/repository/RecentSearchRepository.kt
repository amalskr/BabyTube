package com.ceylonapz.babytube.domain.repository

import com.ceylonapz.babytube.domain.Video
import kotlinx.coroutines.flow.Flow

interface RecentSearchRepository {
    /** Videos from recent searches, newest first. Emits again whenever a search is saved. */
    fun observeRecentVideos(): Flow<List<Video>>

    suspend fun saveSearchResults(videos: List<Video>)
}
