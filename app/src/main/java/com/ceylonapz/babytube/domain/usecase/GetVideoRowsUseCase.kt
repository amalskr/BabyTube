package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetVideoRowsUseCase(
    private val repository: VideoRepository,
    private val categories: List<VideoCategory> = DEFAULT_CATEGORIES,
) {
    /**
     * Loads every category in parallel. Rows that fail or come back empty are skipped,
     * so one bad search doesn't blank the screen; it only fails if no row loads.
     */
    suspend operator fun invoke(): Result<List<VideoRow>> = coroutineScope {
        val results = categories.map { category ->
            async { runCatching { VideoRow(category.title, repository.searchKidsVideos(category.query)) } }
        }.awaitAll()

        val rows = results.mapNotNull { it.getOrNull() }.filter { it.videos.isNotEmpty() }
        when {
            rows.isNotEmpty() -> Result.success(rows)
            else -> results.firstNotNullOfOrNull { it.exceptionOrNull() }
                ?.let { Result.failure(it) }
                ?: Result.success(emptyList())
        }
    }

    companion object {
        // Each category is one YouTube search (~101 quota units of the 10,000/day default).
        val DEFAULT_CATEGORIES = listOf(
            VideoCategory("Nursery Rhymes", "nursery rhymes baby songs"),
            VideoCategory("Lullabies", "lullabies for babies to go to sleep"),
            VideoCategory("Animal Songs", "animal songs for kids"),
            VideoCategory("ABC & Counting", "abc alphabet counting songs for toddlers"),
        )
    }
}
