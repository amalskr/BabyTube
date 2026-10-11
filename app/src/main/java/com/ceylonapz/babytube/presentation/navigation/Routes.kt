package com.ceylonapz.babytube.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object SearchRoute : NavKey

@Serializable
data class PlayerRoute(val videoId: String) : NavKey
