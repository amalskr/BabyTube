package com.ceylonapz.babytube.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ceylonapz.babytube.presentation.home.HomeScreen
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.presentation.player.PlaybackHistory
import com.ceylonapz.babytube.presentation.player.PlayerScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.ceylonapz.babytube.presentation.search.SearchScreen

@Composable
fun BabyTubeNavDisplay() {
    val backStack = rememberNavBackStack(HomeRoute)
    val playbackHistory = koinInject<PlaybackHistory>()
    val lastPlayed by playbackHistory.lastPlayed.collectAsStateWithLifecycle()

    // Only the screen on top restores focus. During the enter animation the previous screen is
    // still composed, and must not use up the record before the player is even visible.
    fun lastPlayedFor(route: NavKey) = lastPlayed.takeIf { backStack.lastOrNull() == route }

    fun play(queue: List<Video>, index: Int, source: String) {
        backStack.add(PlayerRoute(queue.map { it.toQueueVideo() }, index, source))
    }

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
                    onPlay = ::play,
                    onSearchClick = ::openSearch,
                    lastPlayed = lastPlayedFor(HomeRoute),
                    onFocusRestored = playbackHistory::consume,
                )
            }
            entry<SearchRoute> {
                SearchScreen(
                    onPlay = ::play,
                    lastPlayed = lastPlayedFor(SearchRoute),
                    onFocusRestored = playbackHistory::consume,
                )
            }
            entry<PlayerRoute> { route ->
                PlayerScreen(
                    viewModel = koinViewModel {
                        parametersOf(route.queue.map { it.toVideo() }, route.startIndex, route.source)
                    },
                    onExit = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
