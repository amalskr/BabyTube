package com.ceylonapz.babytube.presentation.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

private const val SEEK_STEP_SECONDS = 10f

@Composable
fun PlayerScreen(videoId: String) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val focusRequester = remember { FocusRequester() }
    val tracker = remember { YouTubePlayerTracker() }
    val playerHolder = remember { arrayOfNulls<YouTubePlayer>(1) }

    val playerView = remember {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
            // The WebView can't be driven by the D-pad, so keep focus in Compose and forward keys.
            isFocusable = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            val options = IFramePlayerOptions.Builder(context)
                .controls(1)
                .rel(0) // related videos only from the same channel
                .ivLoadPolicy(3) // hide annotations
                .build()
            initialize(
                object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        playerHolder[0] = youTubePlayer
                        youTubePlayer.addListener(tracker)
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                },
                options,
            )
        }
    }

    // Pauses on background; release() when the screen leaves, since the Activity lives on.
    DisposableEffect(lifecycle, playerView) {
        lifecycle.addObserver(playerView)
        onDispose {
            lifecycle.removeObserver(playerView)
            playerView.release()
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val player = playerHolder[0] ?: return@onKeyEvent false
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionCenter, Key.Enter, Key.MediaPlayPause -> {
                        if (tracker.state == PlayerConstants.PlayerState.PLAYING) player.pause() else player.play()
                        true
                    }
                    Key.DirectionLeft, Key.MediaRewind -> {
                        player.seekTo((tracker.currentSecond - SEEK_STEP_SECONDS).coerceAtLeast(0f))
                        true
                    }
                    Key.DirectionRight, Key.MediaFastForward -> {
                        player.seekTo(tracker.currentSecond + SEEK_STEP_SECONDS)
                        true
                    }
                    else -> false
                }
            },
    ) {
        AndroidView(factory = { playerView }, modifier = Modifier.fillMaxSize())
    }
}
