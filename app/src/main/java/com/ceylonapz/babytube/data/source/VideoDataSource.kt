package com.ceylonapz.babytube.data.source

import com.ceylonapz.babytube.domain.Video

interface VideoDataSource {
    suspend fun fetchVideos(): List<Video>
}
