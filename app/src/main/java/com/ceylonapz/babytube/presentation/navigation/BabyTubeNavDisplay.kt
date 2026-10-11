package com.ceylonapz.babytube.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ceylonapz.babytube.presentation.home.HomeScreen
import com.ceylonapz.babytube.presentation.player.PlayerScreen
import com.ceylonapz.babytube.presentation.search.SearchScreen

@Composable
fun BabyTubeNavDisplay() {
    val backStack = rememberNavBackStack(HomeRoute)

    fun openSearch() {
        if (backStack.lastOrNull() != SearchRoute) backStack.add(SearchRoute)
    }

    NavDisplay(
        backStack = backStack,
        // The remote's Search key (where the TV passes it to apps) opens search from anywhere.
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.key == Key.Search && event.type == KeyEventType.KeyDown) {
                openSearch()
                true
            } else {
                false
            }
        },
        onBack = { backStack.removeLastOrNull() },
        // Gives each screen its own ViewModelStore, cleared when the screen is popped.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    onVideoClick = { video -> backStack.add(PlayerRoute(video.id)) },
                    onSearchClick = ::openSearch,
                )
            }
            entry<SearchRoute> {
                SearchScreen(onVideoClick = { video -> backStack.add(PlayerRoute(video.id)) })
            }
            entry<PlayerRoute> { route ->
                PlayerScreen(videoId = route.videoId)
            }
        },
    )
}
