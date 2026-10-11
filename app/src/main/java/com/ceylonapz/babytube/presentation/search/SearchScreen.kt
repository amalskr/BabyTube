@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.ceylonapz.babytube.presentation.search

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NonInteractiveSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ceylonapz.babytube.R
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.presentation.components.VideoCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onVideoClick: (Video) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Google TV's own voice UI handles the mic, so the app needs no RECORD_AUDIO permission.
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (result.resultCode == Activity.RESULT_OK && !spoken.isNullOrBlank()) {
            viewModel.onVoiceResult(spoken)
        }
    }
    val context = LocalContext.current
    val voiceIntent = remember { voiceIntent(context.packageManager) }

    SearchContent(
        query = query,
        uiState = uiState,
        voiceAvailable = voiceIntent != null,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onVoiceSearch = {
            try {
                voiceIntent?.let(voiceLauncher::launch)
            } catch (_: ActivityNotFoundException) {
                // Recognizer disappeared since we checked; nothing useful to do.
            }
        },
        onVideoClick = onVideoClick,
    )
}

private const val GOOGLE_TV_VOICE_PACKAGE = "com.google.android.katniss"

/**
 * Google TV ships more than one speech recognizer, which would show an app chooser.
 * Pin one instead, preferring Google TV's own voice UI. Null if the device has none.
 */
private fun voiceIntent(packageManager: PackageManager): Intent? {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a song or cartoon name")
    }
    val recognizers = packageManager.queryIntentActivities(intent, 0).map { it.activityInfo.packageName }
    val chosen = recognizers.firstOrNull { it == GOOGLE_TV_VOICE_PACKAGE } ?: recognizers.firstOrNull()
    return chosen?.let { intent.setPackage(it) }
}

@Composable
fun SearchContent(
    query: String,
    uiState: SearchUiState,
    voiceAvailable: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVoiceSearch: () -> Unit,
    onVideoClick: (Video) -> Unit,
) {
    val fieldFocus = remember { FocusRequester() }
    val firstResultFocus = remember { FocusRequester() }
    val hasResults = uiState is SearchUiState.Results
    // Results (new, or on return from the player) take focus; otherwise the field does.
    LaunchedEffect(hasResults) {
        withFrameNanos { } // wait until the grid is laid out
        runCatching { (if (hasResults) firstResultFocus else fieldFocus).requestFocus() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = Color(0xFF141414))
    ) {
        Column(modifier = Modifier.padding(top = 32.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(fieldFocus),
                )
                if (voiceAvailable) {
                    Button(onClick = onVoiceSearch) {
                        Icon(painterResource(R.drawable.ic_mic), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Voice")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (uiState) {
                SearchUiState.Idle -> Message("Type or say what you'd like to watch")
                SearchUiState.Loading -> Message("Searching…")
                is SearchUiState.NoResults -> Message("No kids videos found for \"${uiState.query}\"")
                is SearchUiState.Error -> Message(uiState.message)
                is SearchUiState.Results -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 260.dp),
                    // Padding inside the grid so focused (scaled-up) cards aren't clipped.
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    itemsIndexed(uiState.videos, key = { _, video -> video.id }) { index, video ->
                        VideoCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            modifier = if (index == 0) Modifier.focusRequester(firstResultFocus) else Modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    // TextFieldValue (not String) so the key handler can see where the cursor is.
    var value by remember { mutableStateOf(TextFieldValue(query, TextRange(query.length))) }
    LaunchedEffect(query) {
        // Query replaced from outside (voice search): show it with the cursor at the end.
        if (value.text != query) value = TextFieldValue(query, TextRange(query.length))
    }

    BasicTextField(
        value = value,
        onValueChange = {
            value = it
            onQueryChange(it.text)
        },
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            keyboard?.hide()
            onSearch()
        }),
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            // The text field keeps D-pad keys for its cursor. Let Down leave for the results,
            // and Right leave for the Voice button once the cursor is already at the end.
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val cursorAtEnd = value.selection.collapsed && value.selection.end == value.text.length
                when {
                    event.key == Key.DirectionDown -> {
                        keyboard?.hide()
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                    event.key == Key.DirectionRight && cursorAtEnd -> focusManager.moveFocus(FocusDirection.Right)
                    else -> false
                }
            },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF292929), RoundedCornerShape(28.dp))
                    .border(
                        BorderStroke(3.dp, if (focused) Color.White else Color.Transparent),
                        RoundedCornerShape(28.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Box {
                    if (query.isEmpty()) {
                        Text("Search kids videos", color = Color.Gray, fontSize = 20.sp)
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun Message(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(text = text, color = Color.LightGray, style = MaterialTheme.typography.titleMedium)
    }
}
