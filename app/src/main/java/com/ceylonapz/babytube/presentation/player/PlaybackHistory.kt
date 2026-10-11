package com.ceylonapz.babytube.presentation.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The last video the player showed and which list it was started from (a home row title, or
 * search). Autoplay can move past the card that was clicked, so screens use this to put focus
 * back on the video that actually played last.
 */
data class LastPlayed(val source: String, val videoId: String)

class PlaybackHistory {
    private val _lastPlayed = MutableStateFlow<LastPlayed?>(null)
    val lastPlayed: StateFlow<LastPlayed?> = _lastPlayed.asStateFlow()

    fun record(source: String, videoId: String) {
        _lastPlayed.value = LastPlayed(source, videoId)
    }

    /** Called once a screen has restored focus, so it doesn't happen again. */
    fun consume() {
        _lastPlayed.value = null
    }
}
