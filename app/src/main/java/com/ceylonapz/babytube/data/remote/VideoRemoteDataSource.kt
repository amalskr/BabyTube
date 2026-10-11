package com.ceylonapz.babytube.data.remote

import com.ceylonapz.babytube.data.remote.dto.VideoItemDto

data class SearchPage(val videoIds: List<String>, val nextPageToken: String?)

interface VideoRemoteDataSource {
    suspend fun searchVideos(
        query: String,
        maxResults: Int,
        pageToken: String? = null,
        languageCode: String? = null,
        regionCode: String? = null,
    ): SearchPage

    suspend fun getVideos(ids: List<String>): List<VideoItemDto>
}
