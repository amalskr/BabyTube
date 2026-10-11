package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.RecentSearchRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository

class SearchVideosUseCase(
    private val repository: VideoRepository,
    private val countryRepository: CountryRepository,
    private val recentSearchRepository: RecentSearchRepository,
) {
    suspend operator fun invoke(query: String): Result<List<Video>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Result.success(emptyList())
        return runCatching {
            // Region only limits results to videos viewable here; the words decide the language.
            repository.searchKidsVideos(trimmed, countryRepository.getCountryCode())
                .also { if (it.isNotEmpty()) recentSearchRepository.saveSearchResults(it) }
        }
    }
}
