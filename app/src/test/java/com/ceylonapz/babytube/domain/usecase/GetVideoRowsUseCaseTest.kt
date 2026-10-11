package com.ceylonapz.babytube.domain.usecase

import com.ceylonapz.babytube.domain.Video
import com.ceylonapz.babytube.domain.VideoCategory
import com.ceylonapz.babytube.domain.VideoRow
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetVideoRowsUseCaseTest {

    private val lullaby = Video("1", "Rock-a-bye", "Channel", "")
    private val animal = Video("2", "Old MacDonald", "Channel", "")
    private val categories = listOf(
        VideoCategory("Lullabies", "lullabies"),
        VideoCategory("Animal Songs", "animals"),
    )

    @Test
    fun `builds one row per category in order`() = runTest {
        val repository = FakeRepository(mapOf("lullabies" to listOf(lullaby), "animals" to listOf(animal)))

        val rows = GetVideoRowsUseCase(repository, FakeCountry(null), categories)().getOrThrow()

        assertEquals(
            listOf(VideoRow("Lullabies", listOf(lullaby)), VideoRow("Animal Songs", listOf(animal))),
            rows,
        )
    }

    @Test
    fun `skips failed and empty rows`() = runTest {
        val repository = FakeRepository(mapOf("animals" to listOf(animal)), failing = setOf("lullabies"))

        val rows = GetVideoRowsUseCase(repository, FakeCountry(null), categories)().getOrThrow()

        assertEquals(listOf(VideoRow("Animal Songs", listOf(animal))), rows)
    }

    @Test
    fun `puts the local-language row first for the viewer's country`() = runTest {
        val sinhala = Video("3", "Hawa Giya Kade", "Tikiri", "")
        val repository = FakeRepository(mapOf("sinhala" to listOf(sinhala), "animals" to listOf(animal)))
        val useCase = GetVideoRowsUseCase(
            repository,
            FakeCountry("LK"),
            categories = listOf(VideoCategory("Animal Songs", "animals")),
            countryCategories = mapOf("LK" to VideoCategory("සිංහල ළමා ගීත", "sinhala", "si", "LK")),
        )

        val titles = useCase().getOrThrow().map { it.title }

        assertEquals(listOf("සිංහල ළමා ගීත", "Animal Songs"), titles)
    }

    @Test
    fun `default categories include a Sinhala row for Sri Lanka`() {
        val lk = GetVideoRowsUseCase.COUNTRY_CATEGORIES.getValue("LK")

        assertEquals("si" to "LK", lk.languageCode to lk.regionCode)
    }

    @Test
    fun `fails when every row fails`() = runTest {
        val repository = FakeRepository(emptyMap(), failing = setOf("lullabies", "animals"))

        assertTrue(GetVideoRowsUseCase(repository, FakeCountry(null), categories)().isFailure)
    }

    private class FakeRepository(
        val results: Map<String, List<Video>>,
        val failing: Set<String> = emptySet(),
    ) : VideoRepository {
        override suspend fun searchKidsVideos(category: VideoCategory): List<Video> {
            if (category.query in failing) error("boom")
            return results[category.query].orEmpty()
        }
    }

    private class FakeCountry(val code: String?) : CountryRepository {
        override fun getCountryCode() = code
    }
}
