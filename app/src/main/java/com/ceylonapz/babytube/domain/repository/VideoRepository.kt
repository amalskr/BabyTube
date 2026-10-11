package com.ceylonapz.babytube.domain.repository

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory

/** Every method returns only videos that are made for kids and can be played inside the app. */
interface VideoRepository {
    /** Each call moves on to the next page of results, so repeated calls return new videos. */
    suspend fun getCategoryVideos(category: VideoCategory): List<Video>

    /** One-off search for what the viewer typed or said; always the best matches. */
    suspend fun searchKidsVideos(query: String, regionCode: String?): List<Video>
}
