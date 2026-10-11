package com.ceylonapz.babytube.data.remote

import com.ceylonapz.babytube.data.remote.dto.ErrorResponseDto
import com.ceylonapz.babytube.data.remote.dto.SearchResponseDto
import com.ceylonapz.babytube.data.remote.dto.VideoItemDto
import com.ceylonapz.babytube.data.remote.dto.VideoListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class YouTubeRemoteDataSource(
    private val client: HttpClient,
    private val apiKey: String,
) : VideoRemoteDataSource {

    // search.list costs 100 quota units per call (videos.list costs 1), out of 10,000/day by default.
    override suspend fun searchVideoIds(query: String, maxResults: Int): List<String> =
        request<SearchResponseDto>("search") {
            parameter("part", "id")
            parameter("type", "video")
            parameter("q", query)
            parameter("safeSearch", "strict")
            parameter("videoEmbeddable", "true")
            parameter("maxResults", maxResults)
        }.items.mapNotNull { it.id.videoId }

    override suspend fun getVideos(ids: List<String>): List<VideoItemDto> =
        request<VideoListResponseDto>("videos") {
            parameter("part", "snippet,status")
            parameter("id", ids.joinToString(","))
        }.items

    private suspend inline fun <reified T> request(
        path: String,
        block: HttpRequestBuilder.() -> Unit,
    ): T {
        if (apiKey.isBlank()) {
            throw YouTubeApiException("YouTube API key missing. Add YOUTUBE_API_KEY to local.properties.")
        }
        val response = client.get(path) {
            header(API_KEY_HEADER, apiKey)
            block()
        }
        if (!response.status.isSuccess()) throw response.toApiException()
        return response.body()
    }

    private suspend fun HttpResponse.toApiException(): YouTubeApiException {
        val message = runCatching { body<ErrorResponseDto>().error.message }
            .getOrDefault("YouTube request failed (${status.value})")
        return YouTubeApiException(message)
    }
}
