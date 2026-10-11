package com.ceylonapz.babytube.di

import com.ceylonapz.babytube.BuildConfig
import com.ceylonapz.babytube.data.remote.VideoRemoteDataSource
import com.ceylonapz.babytube.data.remote.YouTubeRemoteDataSource
import com.ceylonapz.babytube.data.remote.createHttpClient
import com.ceylonapz.babytube.data.repository.VideoRepositoryImpl
import com.ceylonapz.babytube.domain.repository.VideoRepository
import com.ceylonapz.babytube.domain.usecase.GetVideoRowsUseCase
import com.ceylonapz.babytube.presentation.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// The single place where data implementations are bound to domain contracts.
val dataModule = module {
    single<HttpClient> { createHttpClient(Android.create(), enableLogging = BuildConfig.DEBUG) }
    single<VideoRemoteDataSource> { YouTubeRemoteDataSource(get(), BuildConfig.YOUTUBE_API_KEY) }
    single<VideoRepository> { VideoRepositoryImpl(get(), Dispatchers.IO) }
}

val domainModule = module {
    factory { GetVideoRowsUseCase(get()) }
}

val presentationModule = module {
    viewModelOf(::HomeViewModel)
}

val appModules = listOf(dataModule, domainModule, presentationModule)
