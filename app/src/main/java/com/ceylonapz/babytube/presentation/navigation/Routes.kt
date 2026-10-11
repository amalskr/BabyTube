package com.ceylonapz.babytube.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.ceylonapz.babytube.domain.Video
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object SearchRoute : NavKey

/**
 * Plays [queue] from [startIndex]. [source] names the list it came from (a home row title or
 * search) so focus can return to the last-played card.
 */
@Serializable
data class PlayerRoute(
    val queue: List<QueueVideo>,
    val startIndex: Int,
    val source: String,
) : NavKey

/** Serializable copy of [Video], so the queue survives process death with the back stack. */
@Serializable
data class QueueVideo(val id: String, val title: String, val channel: String, val thumbnailUrl: String)

fun Video.toQueueVideo() = QueueVideo(id, title, channel, thumbnailUrl)

fun QueueVideo.toVideo() = Video(id, title, channel, thumbnailUrl)
