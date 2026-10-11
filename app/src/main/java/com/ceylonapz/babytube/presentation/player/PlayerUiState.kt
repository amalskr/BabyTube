package com.ceylonapz.babytube.presentation.player

import com.ceylonapz.babytube.domain.Video

data class PlayerUiState(
    val video: Video,
    val previousVideo: Video?,
    val nextVideo: Video?,
    /** Changes every time a video should (re)start from the beginning. */
    val playRequest: Int = 0,
    val isPlaying: Boolean = false,
    val positionSeconds: Float = 0f,
    val durationSeconds: Float = 0f,
    val controlsVisible: Boolean = false,
    val end: PlayerEnd? = null,
)

/** What to show over the player once a video finishes, instead of YouTube's suggestions. */
sealed interface PlayerEnd {
    data class UpNext(val next: Video, val secondsLeft: Int) : PlayerEnd
    data object Finished : PlayerEnd
}
