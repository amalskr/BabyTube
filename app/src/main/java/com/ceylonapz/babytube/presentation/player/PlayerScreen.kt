@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.ceylonapz.babytube.presentation.player

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import com.ceylonapz.babytube.R
import com.ceylonapz.babytube.domain.Video
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

private const val SEEK_STEP_SECONDS = 10f
private const val SEEK_BAR_TIMEOUT_MS = 2_000L
private val Pink = Color(0xFFED5C8F)

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }

    val rootFocus = remember { FocusRequester() }
    val playPauseFocus = remember { FocusRequester() }
    val endFocus = remember { FocusRequester() }

    val playerView = remember {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
            // The WebView can't be driven by the D-pad, so keep focus in Compose and forward keys.
            isFocusable = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            val options = IFramePlayerOptions.Builder(context)
                .controls(0) // our own remote-friendly controls replace YouTube's
                .rel(0) // related videos only from the same channel
                .ivLoadPolicy(3) // hide annotations
                .build()
            initialize(
                object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer
                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        when (state) {
                            PlayerConstants.PlayerState.PLAYING -> viewModel.onPlayingChanged(true)
                            PlayerConstants.PlayerState.PAUSED -> viewModel.onPlayingChanged(false)
                            PlayerConstants.PlayerState.ENDED -> viewModel.onVideoEnded()
                            else -> Unit
                        }
                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) =
                        viewModel.onProgress(second)

                    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) =
                        viewModel.onDuration(duration)
                },
                options,
            )
        }
    }

    // (Re)starts playback whenever the queue moves on or a replay is requested.
    LaunchedEffect(player, state.video.id, state.playRequest) {
        player?.loadVideo(state.video.id, 0f)
    }

    // Pauses on background; release() when the screen leaves, since the Activity lives on.
    DisposableEffect(lifecycle, playerView) {
        lifecycle.addObserver(playerView)
        onDispose {
            lifecycle.removeObserver(playerView)
            playerView.release()
        }
    }

    // Focus follows what's on screen: end screen buttons, the controls, or the bare player.
    LaunchedEffect(state.controlsVisible, state.end != null) {
        withFrameNanos { }
        runCatching {
            when {
                state.end != null -> endFocus.requestFocus()
                state.controlsVisible -> playPauseFocus.requestFocus()
                else -> rootFocus.requestFocus()
            }
        }
    }

    // Seeking shows just a slim progress bar (focus stays on the video so repeated presses
    // keep seeking); it fades a moment after the last press.
    var seekBarToken by remember { mutableStateOf(0) }
    LaunchedEffect(seekBarToken) {
        if (seekBarToken > 0) {
            delay(SEEK_BAR_TIMEOUT_MS)
            seekBarToken = 0
        }
    }

    // First Back just hides the controls; the next one leaves the player.
    BackHandler(enabled = state.controlsVisible) { viewModel.hideControls() }

    fun togglePlayPause() {
        val p = player ?: return
        if (state.isPlaying) p.pause() else p.play()
    }

    fun seekBy(seconds: Float) {
        val p = player ?: return
        val max = state.durationSeconds.takeIf { it > 0f } ?: Float.MAX_VALUE
        p.seekTo((state.positionSeconds + seconds).coerceIn(0f, max))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocus)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                viewModel.onUserInteraction()
                // Media keys work everywhere, whatever has focus.
                when (event.key) {
                    Key.MediaPlayPause -> togglePlayPause().let { true }
                    Key.MediaNext -> viewModel.playNext().let { true }
                    Key.MediaPrevious -> viewModel.playPrevious().let { true }
                    else -> false
                }
            }
            // Only while the bare video is showing; otherwise the D-pad moves between buttons.
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || state.controlsVisible || state.end != null) {
                    return@onKeyEvent false
                }
                when (event.key) {
                    Key.DirectionCenter, Key.Enter -> {
                        togglePlayPause()
                        viewModel.showControls()
                    }
                    Key.DirectionUp, Key.DirectionDown -> viewModel.showControls()
                    Key.DirectionLeft, Key.MediaRewind -> {
                        seekBy(-SEEK_STEP_SECONDS)
                        seekBarToken++
                    }
                    Key.DirectionRight, Key.MediaFastForward -> {
                        seekBy(SEEK_STEP_SECONDS)
                        seekBarToken++
                    }
                    else -> return@onKeyEvent false
                }
                true
            },
    ) {
        AndroidView(factory = { playerView }, modifier = Modifier.fillMaxSize())

        if (seekBarToken > 0 && !state.controlsVisible && state.end == null) {
            SeekBar(state = state, modifier = Modifier.align(Alignment.BottomCenter))
        }

        if (state.controlsVisible) {
            ControlsOverlay(
                state = state,
                playPauseFocus = playPauseFocus,
                onPrevious = viewModel::playPrevious,
                onPlayPause = ::togglePlayPause,
                onNext = viewModel::playNext,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        when (val end = state.end) {
            is PlayerEnd.UpNext -> UpNextOverlay(
                end = end,
                focusRequester = endFocus,
                onPlayNow = viewModel::playNext,
                onReplay = viewModel::replay,
            )
            PlayerEnd.Finished -> FinishedOverlay(
                focusRequester = endFocus,
                onReplay = viewModel::replay,
                onExit = onExit,
            )
            null -> Unit
        }
    }
}

@Composable
private fun ControlsOverlay(
    state: PlayerUiState,
    playPauseFocus: FocusRequester,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
            .padding(horizontal = 48.dp, vertical = 32.dp),
    ) {
        Text(state.video.title, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(state.video.channel, color = Color.LightGray, maxLines = 1)
        Spacer(Modifier.height(16.dp))

        ProgressBar(fraction = state.progressFraction())
        Spacer(Modifier.height(6.dp))
        Text(
            "${formatTime(state.positionSeconds)} / ${formatTime(state.durationSeconds)}",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onPrevious, enabled = state.previousVideo != null) {
                Icon(painterResource(R.drawable.ic_skip_previous), contentDescription = "Previous video")
            }
            Button(onClick = onPlayPause, modifier = Modifier.focusRequester(playPauseFocus)) {
                Icon(
                    painterResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                )
            }
            Button(onClick = onNext, enabled = state.nextVideo != null) {
                Icon(painterResource(R.drawable.ic_skip_next), contentDescription = "Next video")
            }
        }

        state.nextVideo?.let { next ->
            Spacer(Modifier.height(12.dp))
            Text(
                "Up next: ${next.title}",
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun SeekBar(state: PlayerUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
            .padding(horizontal = 48.dp, vertical = 32.dp),
    ) {
        ProgressBar(fraction = state.progressFraction())
        Spacer(Modifier.height(6.dp))
        Text(
            "${formatTime(state.positionSeconds)} / ${formatTime(state.durationSeconds)}",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun PlayerUiState.progressFraction() =
    if (durationSeconds > 0f) positionSeconds / durationSeconds else 0f

@Composable
private fun ProgressBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .background(Pink),
        )
    }
}

// Opaque, so YouTube's own end screen of suggestions never shows through.
@Composable
private fun UpNextOverlay(
    end: PlayerEnd.UpNext,
    focusRequester: FocusRequester,
    onPlayNow: () -> Unit,
    onReplay: () -> Unit,
) {
    EndScreen {
        AsyncImage(
            model = end.next.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 384.dp, height = 216.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF292929)),
        )
        Spacer(Modifier.width(40.dp))
        Column(modifier = Modifier.width(420.dp)) {
            Text("Up next", color = Pink, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(end.next.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(end.next.channel, color = Color.LightGray, maxLines = 1)
            Spacer(Modifier.height(16.dp))
            Text("Playing in ${end.secondsLeft}…", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onPlayNow, modifier = Modifier.focusRequester(focusRequester)) {
                    Icon(painterResource(R.drawable.ic_play), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play now")
                }
                Button(onClick = onReplay) {
                    Icon(painterResource(R.drawable.ic_replay), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Watch again")
                }
            }
        }
    }
}

@Composable
private fun FinishedOverlay(
    focusRequester: FocusRequester,
    onReplay: () -> Unit,
    onExit: () -> Unit,
) {
    EndScreen {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("That's all for now!", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onReplay, modifier = Modifier.focusRequester(focusRequester)) {
                    Icon(painterResource(R.drawable.ic_replay), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Watch again")
                }
                Button(onClick = onExit) {
                    Text("Back to videos")
                }
            }
        }
    }
}

@Composable
private fun EndScreen(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

private fun formatTime(seconds: Float): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val h = total / 3600
    val m = total % 3600 / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
