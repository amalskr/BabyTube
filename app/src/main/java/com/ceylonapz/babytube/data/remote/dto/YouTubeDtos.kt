package com.ceylonapz.babytube.data.remote.dto

import kotlinx.serialization.Serializable

// Only the fields the app uses; the JSON parser ignores the rest.

@Serializable
data class SearchResponseDto(val items: List<SearchItemDto> = emptyList())

@Serializable
data class SearchItemDto(val id: SearchIdDto)

@Serializable
data class SearchIdDto(val videoId: String? = null)

@Serializable
data class VideoListResponseDto(val items: List<VideoItemDto> = emptyList())

@Serializable
data class VideoItemDto(
    val id: String,
    val snippet: SnippetDto,
    val status: StatusDto,
)

@Serializable
data class SnippetDto(
    val title: String,
    val channelTitle: String,
    val thumbnails: ThumbnailsDto,
)

@Serializable
data class ThumbnailsDto(
    val default: ThumbnailDto? = null,
    val medium: ThumbnailDto? = null,
    val high: ThumbnailDto? = null,
)

@Serializable
data class ThumbnailDto(val url: String)

@Serializable
data class StatusDto(
    val embeddable: Boolean = false,
    val madeForKids: Boolean = false,
)

@Serializable
data class ErrorResponseDto(val error: ErrorDto)

@Serializable
data class ErrorDto(val code: Int, val message: String)
