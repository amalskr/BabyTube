package com.ceylonapz.babytube.domain.repository

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory

interface VideoRepository {
    /**
     * Returns only videos that are made for kids and can be played inside the app.
     * Each call moves on to the next page of results, so repeated calls return new videos.
     */
    suspend fun searchKidsVideos(category: VideoCategory): List<Video>
}
