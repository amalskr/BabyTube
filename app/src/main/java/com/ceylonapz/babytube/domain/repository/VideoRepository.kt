package com.ceylonapz.babytube.domain.repository

import com.ceylonapz.babytube.domain.Video

interface VideoRepository {
    /** Returns only videos that are made for kids and can be played inside the app. */
    suspend fun searchKidsVideos(query: String): List<Video>
}
