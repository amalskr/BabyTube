package com.ceylonapz.babytube.domain.repository

import com.ceylonapz.babytube.domain.Video

interface VideoRepository {
    suspend fun getVideos(): List<Video>
}
