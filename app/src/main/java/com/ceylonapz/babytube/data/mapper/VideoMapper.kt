package com.ceylonapz.babytube.data.mapper

import com.ceylonapz.babytube.data.remote.dto.VideoItemDto
import com.ceylonapz.babytube.domain.Video

fun VideoItemDto.toDomain(): Video = Video(
    id = id,
    title = snippet.title,
    channel = snippet.channelTitle,
    thumbnailUrl = with(snippet.thumbnails) { high ?: medium ?: default }?.url.orEmpty(),
)
