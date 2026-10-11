package com.ceylonapz.babytube.domain

data class VideoCategory(val title: String, val query: String)

data class VideoRow(val title: String, val videos: List<Video>)
