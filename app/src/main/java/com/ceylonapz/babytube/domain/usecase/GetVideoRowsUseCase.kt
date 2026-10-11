package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetVideoRowsUseCase(
    private val repository: VideoRepository,
    private val countryRepository: CountryRepository,
    private val categories: List<VideoCategory> = DEFAULT_CATEGORIES,
    private val countryCategories: Map<String, VideoCategory> = COUNTRY_CATEGORIES,
) {
    /**
     * Loads every category in parallel, with the viewer's local-language row first.
     * Rows that fail or come back empty are skipped, so one bad search doesn't blank
     * the screen; it only fails if no row loads.
     */
    suspend operator fun invoke(): Result<List<VideoRow>> = coroutineScope {
        val countryCategory = countryRepository.getCountryCode()?.let { countryCategories[it] }
        val results = (listOfNotNull(countryCategory) + categories).map { category ->
            async { runCatching { VideoRow(category.title, repository.searchKidsVideos(category)) } }
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

        // Extra local-language row, keyed by country code. Add a country by adding an entry.
        val COUNTRY_CATEGORIES = mapOf(
            "LK" to VideoCategory("සිංහල ළමා ගීත", "සිංහල ළමා ගීත", languageCode = "si", regionCode = "LK"),
            "IN" to VideoCategory("हिंदी बाल गीत", "hindi rhymes for kids", languageCode = "hi", regionCode = "IN"),
        )
    }
}
