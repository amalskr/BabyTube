package com.ceylonapz.babytube.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ceylonapz.babytube.presentation.home.HomeScreen
import com.ceylonapz.babytube.presentation.player.PlayerScreen

@Composable
fun BabyTubeNavDisplay() {
    val backStack = rememberNavBackStack(HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        // Gives each screen its own ViewModelStore, cleared when the screen is popped.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(onVideoClick = { video -> backStack.add(PlayerRoute(video.id)) })
            }
            entry<PlayerRoute> { route ->
                PlayerScreen(videoId = route.videoId)
            }
        },
    )
}
