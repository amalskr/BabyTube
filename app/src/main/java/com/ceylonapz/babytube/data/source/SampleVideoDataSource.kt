package com.ceylonapz.babytube.data.source

import com.ceylonapz.babytube.domain.Video

// Placeholder until a real remote/local source is wired in.
class SampleVideoDataSource : VideoDataSource {
    override suspend fun fetchVideos(): List<Video> = listOf(
        Video(
            id = "1",
            title = "Introduction to Kotlin",
            channel = "Code Academy",
            thumbnailUrl = "https://example.com/1.jpg",
            videoUrl = ""
        ),
        Video(
            id = "2",
            title = "Jetpack Compose Tutorial",
            channel = "Android Dev",
            thumbnailUrl = "https://example.com/2.jpg",
            videoUrl = ""
        ),
        Video(
            id = "3",
            title = "Android TV Development",
            channel = "Tech World",
            thumbnailUrl = "https://example.com/3.jpg",
            videoUrl = ""
        )
    )
}
