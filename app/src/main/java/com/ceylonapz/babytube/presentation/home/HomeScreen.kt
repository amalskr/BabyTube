@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.ceylonapz.babytube.presentation.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.NonInteractiveSurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.ceylonapz.babytube.R
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.presentation.components.VideoCard
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.presentation.theme.BabyTubeTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onVideoClick: (Video) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onRetry = viewModel::loadVideos,
        onVideoClick = onVideoClick,
        onSearchClick = onSearchClick,
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onSearchClick: () -> Unit,
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

            is HomeUiState.Success -> VideoRows(rows = uiState.rows, onVideoClick = onVideoClick, onSearchClick = onSearchClick)
        }
    }
}

@Composable
private fun VideoRows(rows: List<VideoRow>, onVideoClick: (Video) -> Unit, onSearchClick: () -> Unit) {
    LazyColumn(
        // Padding goes inside the lists so focused (scaled-up) cards aren't clipped.
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discover Videos",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Button(onClick = onSearchClick) {
                    Icon(painterResource(R.drawable.ic_search), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search")
                }
            }
        }

        items(items = rows, key = { it.title }) { row ->
            VideoRowSection(row = row, onVideoClick = onVideoClick)
        }
    }
}

@Composable
private fun VideoRowSection(row: VideoRow, onVideoClick: (Video) -> Unit) {
    Column {
        Text(
            text = row.title,
            modifier = Modifier.padding(horizontal = 32.dp),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = row.videos,
                key = { it.id }
            ) { video ->
                VideoCard(video = video, onClick = { onVideoClick(video) })
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
                listOf(
                    VideoRow("Nursery Rhymes", listOf(Video("1", "Twinkle Twinkle Little Star", "Nursery Rhymes", ""))),
                    VideoRow("Lullabies", listOf(Video("2", "Rock-a-bye Baby", "Lullabies", ""))),
                )
            ),
            onRetry = {},
            onVideoClick = {},
            onSearchClick = {},
        )
    }
}
