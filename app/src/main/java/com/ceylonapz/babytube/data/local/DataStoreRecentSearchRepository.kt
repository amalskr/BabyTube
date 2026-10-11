package com.ceylonapz.babytube.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Stored copy of a video, so the row needs no YouTube quota to show. */
@Serializable
private data class StoredVideo(val id: String, val title: String, val channel: String, val thumbnailUrl: String)

class DataStoreRecentSearchRepository(
    private val dataStore: DataStore<Preferences>,
) : RecentSearchRepository {

    override fun observeRecentVideos(): Flow<List<Video>> =
        dataStore.data.map { prefs -> decode(prefs[RECENT_VIDEOS]) }

    // Newest search goes first; older results fill the rest, without duplicates.
    override suspend fun saveSearchResults(videos: List<Video>) {
        dataStore.edit { prefs ->
            val merged = (videos + decode(prefs[RECENT_VIDEOS]))
                .distinctBy { it.id }
                .take(MAX_VIDEOS)
            prefs[RECENT_VIDEOS] = Json.encodeToString(merged.map { StoredVideo(it.id, it.title, it.channel, it.thumbnailUrl) })
        }
    }

    private fun decode(json: String?): List<Video> =
        json?.let { runCatching { Json.decodeFromString<List<StoredVideo>>(it) }.getOrNull() }
            .orEmpty()
            .map { Video(it.id, it.title, it.channel, it.thumbnailUrl) }

    private companion object {
        val RECENT_VIDEOS = stringPreferencesKey("recent_search_videos")
        const val MAX_VIDEOS = 20
    }
}
