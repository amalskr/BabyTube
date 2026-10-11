package com.ceylonapz.babytube.data.repository

import com.ceylonapz.babytube.data.local.SearchCursor
import com.ceylonapz.babytube.data.local.SearchCursorStore
import com.ceylonapz.babytube.data.mapper.toDomain
import com.ceylonapz.babytube.data.remote.SearchPage
import com.ceylonapz.babytube.data.remote.VideoRemoteDataSource
import com.ceylonapz.babytube.data.remote.YouTubeApiException
import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VideoRepositoryImpl(
    private val remoteDataSource: VideoRemoteDataSource,
    private val cursorStore: SearchCursorStore,
    private val ioDispatcher: CoroutineDispatcher,
) : VideoRepository {

    override suspend fun getCategoryVideos(category: VideoCategory): List<Video> = withContext(ioDispatcher) {
        val key = category.cursorKey()
        val cursor = cursorStore.get(key)
        val page = search(category, cursor?.pageToken)

        // Remember where to continue, so the next launch shows the next page of videos.
        // Wrap back to page 1 after MAX_PAGES: deeper pages get less relevant.
        val nextIndex = (cursor?.pageIndex ?: 0) + 1
        val nextToken = page.nextPageToken
        cursorStore.set(key, if (nextToken != null && nextIndex < MAX_PAGES) SearchCursor(nextToken, nextIndex) else null)

        kidsVideos(page.videoIds)
    }

    override suspend fun searchKidsVideos(query: String, regionCode: String?): List<Video> =
        withContext(ioDispatcher) {
            val page = remoteDataSource.searchVideos(query, MAX_RESULTS, regionCode = regionCode)
            kidsVideos(page.videoIds)
        }

    // search.list can't filter on madeForKids, so check each video's status.
    private suspend fun kidsVideos(ids: List<String>): List<Video> {
        if (ids.isEmpty()) return emptyList()
        return remoteDataSource.getVideos(ids)
            .filter { it.status.madeForKids && it.status.embeddable }
            .map { it.toDomain() }
    }

    private suspend fun search(category: VideoCategory, pageToken: String?): SearchPage = try {
        searchPage(category, pageToken)
    } catch (e: YouTubeApiException) {
        // A saved token can expire; start over from page 1 rather than failing the row.
        if (pageToken != null && e.reason == INVALID_PAGE_TOKEN) searchPage(category, null) else throw e
    }

    private suspend fun searchPage(category: VideoCategory, pageToken: String?) =
        remoteDataSource.searchVideos(
            query = category.query,
            maxResults = MAX_RESULTS,
            pageToken = pageToken,
            languageCode = category.languageCode,
            regionCode = category.regionCode,
        )

    private fun VideoCategory.cursorKey() = listOf(query, languageCode, regionCode).joinToString("|")

    private companion object {
        const val MAX_RESULTS = 25
        const val MAX_PAGES = 5
        const val INVALID_PAGE_TOKEN = "invalidPageToken"
    }
}
