package com.ceylonapz.babytube.data.remote

import com.ceylonapz.babytube.data.remote.dto.VideoItemDto

interface VideoRemoteDataSource {
    suspend fun searchVideoIds(query: String, maxResults: Int): List<String>

    suspend fun getVideos(ids: List<String>): List<VideoItemDto>
}
