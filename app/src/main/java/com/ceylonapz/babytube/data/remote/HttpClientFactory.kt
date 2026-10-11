package com.ceylonapz.babytube.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
const val API_KEY_HEADER = "X-Goog-Api-Key"

fun createHttpClient(engine: HttpClientEngine, enableLogging: Boolean): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        if (enableLogging) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
                    }
                }
                sanitizeHeader { it == API_KEY_HEADER }
            }
        }
        defaultRequest { url(YOUTUBE_BASE_URL) }
    }
