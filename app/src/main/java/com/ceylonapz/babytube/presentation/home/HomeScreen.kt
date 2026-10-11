@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.ceylonapz.babytube.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.NonInteractiveSurfaceDefaults
import androidx.tv.material3.Text
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.presentation.theme.BabyTubeTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState, onRetry = viewModel::loadVideos)
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = Color(0xFF141414))
    ) {
        when (uiState) {
            HomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Loading…", color = Color.White)
            }

            is HomeUiState.Error -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = uiState.message, color = Color.White)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }

            is HomeUiState.Success -> VideoList(videos = uiState.videos)
        }
    }
}

@Composable
private fun VideoList(videos: List<Video>) {
    LazyColumn(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Discover Videos",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }

        item {
            Text(
                text = "Recommended",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(
                    items = videos,
                    key = { it.id }
                ) { video ->
                    VideoCard(video = video)
                }
            }
        }
    }
}

@Composable
fun VideoCard(video: Video) {
    // TV Card tracks focus itself; the white border shows only while focused.
    Card(
        modifier = Modifier.width(260.dp),
        shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
        border = CardDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, Color.White))
        ),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF292929)
        ),
        onClick = {
            // Step 5: Open video player
        }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    colors = NonInteractiveSurfaceDefaults.colors(containerColor = Color(0xFF424242))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    maxLines = 2
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = video.channel,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun HomeContentPreview() {
    BabyTubeTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                listOf(Video("1", "Introduction to Kotlin", "Code Academy", "", ""))
            ),
            onRetry = {},
        )
    }
}
