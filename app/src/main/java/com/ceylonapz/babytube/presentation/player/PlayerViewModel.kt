package com.ceylonapz.babytube.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceylonapz.babytube.domain.Video
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Plays [queue] in order starting at [startIndex]: the rest of the row (or search results)
 * follows automatically, which is what babies need since they don't navigate.
 */
class PlayerViewModel(
    private val queue: List<Video>,
    startIndex: Int,
    private val source: String,
    private val history: PlaybackHistory,
) : ViewModel() {

    private var index = startIndex.coerceIn(queue.indices)
    private val _uiState = MutableStateFlow(stateFor(index, playRequest = 0))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var hideControlsJob: Job? = null
    private var countdownJob: Job? = null

    // Where the last seek is heading. The player reports progress with a delay, so quick
    // repeated presses must build on this, not on the last reported position.
    private var pendingSeek: Float? = null
    private var pendingSeekJob: Job? = null

    init {
        history.record(source, queue[index].id)
    }

    fun playNext() = playAt(index + 1)

    fun playPrevious() = playAt(index - 1)

    fun replay() = playAt(index)

    fun onPlayingChanged(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
        // Keep controls up while paused; hide them again once playback resumes.
        if (playing && _uiState.value.controlsVisible) scheduleHideControls() else hideControlsJob?.cancel()
    }

    fun onProgress(positionSeconds: Float) {
        val pending = pendingSeek
        if (pending != null) {
            // Ignore stale reports from before the seek took effect.
            if (abs(positionSeconds - pending) > SEEK_TOLERANCE_SECONDS) return
            clearPendingSeek()
        }
        _uiState.update { it.copy(positionSeconds = positionSeconds) }
    }

    /** Returns the position to seek the player to, [deltaSeconds] from the current target. */
    fun seekBy(deltaSeconds: Float): Float {
        val state = _uiState.value
        val max = state.durationSeconds.takeIf { it > 0f } ?: Float.MAX_VALUE
        val target = ((pendingSeek ?: state.positionSeconds) + deltaSeconds).coerceIn(0f, max)
        pendingSeek = target
        _uiState.update { it.copy(positionSeconds = target) }
        pendingSeekJob?.cancel()
        pendingSeekJob = viewModelScope.launch {
            delay(PENDING_SEEK_TIMEOUT_MS)
            pendingSeek = null
        }
        return target
    }

    private fun clearPendingSeek() {
        pendingSeekJob?.cancel()
        pendingSeek = null
    }

    fun onDuration(durationSeconds: Float) = _uiState.update { it.copy(durationSeconds = durationSeconds) }

    fun onVideoEnded() {
        hideControlsJob?.cancel()
        val next = queue.getOrNull(index + 1)
        if (next == null) {
            _uiState.update { it.copy(isPlaying = false, controlsVisible = false, end = PlayerEnd.Finished) }
            return
        }
        _uiState.update {
            it.copy(isPlaying = false, controlsVisible = false, end = PlayerEnd.UpNext(next, UP_NEXT_SECONDS))
        }
        countdownJob = viewModelScope.launch {
            for (secondsLeft in UP_NEXT_SECONDS - 1 downTo 0) {
                delay(1_000)
                _uiState.update { it.copy(end = PlayerEnd.UpNext(next, secondsLeft)) }
            }
            playNext()
        }
    }

    fun showControls() {
        if (_uiState.value.end != null) return
        _uiState.update { it.copy(controlsVisible = true) }
        if (_uiState.value.isPlaying) scheduleHideControls()
    }

    /** Any remote key while the controls are up keeps them up a little longer. */
    fun onUserInteraction() {
        if (_uiState.value.controlsVisible && _uiState.value.isPlaying) scheduleHideControls()
    }

    fun hideControls() {
        hideControlsJob?.cancel()
        _uiState.update { it.copy(controlsVisible = false) }
    }

    private fun playAt(newIndex: Int) {
        if (newIndex !in queue.indices) return
        countdownJob?.cancel()
        clearPendingSeek()
        index = newIndex
        history.record(source, queue[index].id)
        val keepControls = _uiState.value.controlsVisible
        _uiState.value = stateFor(index, playRequest = _uiState.value.playRequest + 1)
            .copy(controlsVisible = keepControls)
        if (keepControls) scheduleHideControls()
    }

    private fun scheduleHideControls() {
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(CONTROLS_TIMEOUT_MS)
            _uiState.update { it.copy(controlsVisible = false) }
        }
    }

    private fun stateFor(index: Int, playRequest: Int) = PlayerUiState(
        video = queue[index],
        previousVideo = queue.getOrNull(index - 1),
        nextVideo = queue.getOrNull(index + 1),
        playRequest = playRequest,
    )

    companion object {
        const val UP_NEXT_SECONDS = 5
        const val CONTROLS_TIMEOUT_MS = 5_000L
        private const val SEEK_TOLERANCE_SECONDS = 2f
        private const val PENDING_SEEK_TIMEOUT_MS = 2_000L
    }
}
