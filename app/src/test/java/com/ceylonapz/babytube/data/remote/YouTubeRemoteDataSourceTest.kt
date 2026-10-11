package com.ceylonapz.babytube.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeRemoteDataSourceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(status: HttpStatusCode, body: String, apiKey: String = "test-key") =
        YouTubeRemoteDataSource(
            client = createHttpClient(
                MockEngine { request ->
                    requests += request
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
                enableLogging = false,
            ),
            apiKey = apiKey,
        )

    @Test
    fun `search sends kid-safe parameters and parses video ids`() = runTest {
        val body = """{"nextPageToken":"CBkQAA","items":[{"id":{"kind":"youtube#video","videoId":"abc"}},{"id":{"videoId":"def"}}]}"""

        val page = dataSource(HttpStatusCode.OK, body).searchVideos("baby songs", 25)

        assertEquals(SearchPage(listOf("abc", "def"), "CBkQAA"), page)
        val request = requests.single()
        assertEquals("https://www.googleapis.com/youtube/v3/search", request.url.toString().substringBefore("?"))
        assertEquals("strict", request.url.parameters["safeSearch"])
        assertEquals("true", request.url.parameters["videoEmbeddable"])
        assertEquals("baby songs", request.url.parameters["q"])
        assertEquals("test-key", request.headers[API_KEY_HEADER])
        assertTrue("key must not be in the URL", request.url.parameters["key"] == null)
        assertNull(request.url.parameters["pageToken"])
        assertNull(request.url.parameters["regionCode"])
    }

    @Test
    fun `search sends page token, language and region when given`() = runTest {
        dataSource(HttpStatusCode.OK, "{}").searchVideos("q", 25, pageToken = "CBkQAA", languageCode = "si", regionCode = "LK")

        val params = requests.single().url.parameters
        assertEquals("CBkQAA", params["pageToken"])
        assertEquals("si", params["relevanceLanguage"])
        assertEquals("LK", params["regionCode"])
    }

    @Test
    fun `videos parses snippet and status`() = runTest {
        val body = """{"items":[{"id":"abc","snippet":{"title":"Twinkle","channelTitle":"Rhymes",
            "thumbnails":{"high":{"url":"https://i.ytimg.com/abc.jpg","width":480}}},
            "status":{"embeddable":true,"madeForKids":true}}]}"""

        val video = dataSource(HttpStatusCode.OK, body).getVideos(listOf("abc", "def")).single()

        assertEquals("abc,def", requests.single().url.parameters["id"])
        assertEquals("Twinkle", video.snippet.title)
        assertTrue(video.status.madeForKids)
    }

    @Test
    fun `api error message is surfaced`() = runTest {
        val body = """{"error":{"code":403,"message":"quota exceeded","errors":[{"reason":"quotaExceeded"}]}}"""

        val error = runCatching { dataSource(HttpStatusCode.Forbidden, body).searchVideos("q", 1) }
            .exceptionOrNull()

        assertTrue(error is YouTubeApiException)
        assertEquals("quota exceeded", error?.message)
        assertEquals("quotaExceeded", (error as YouTubeApiException).reason)
    }

    @Test
    fun `missing api key fails without calling the network`() {
        assertThrows(YouTubeApiException::class.java) {
            kotlinx.coroutines.runBlocking { dataSource(HttpStatusCode.OK, "{}", apiKey = "").searchVideos("q", 1) }
        }
        assertTrue(requests.isEmpty())
    }
}
