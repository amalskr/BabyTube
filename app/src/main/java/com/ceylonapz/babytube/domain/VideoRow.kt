package com.ceylonapz.babytube.domain

data class VideoCategory(
    val title: String,
    val query: String,
    /** ISO 639-1 code, e.g. "si". Biases results towards this language. */
    val languageCode: String? = null,
    /** ISO 3166-1 alpha-2 code, e.g. "LK". Returns videos viewable in this country. */
    val regionCode: String? = null,
)

data class VideoRow(val title: String, val videos: List<Video>)
