package com.ceylonapz.babytube.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.ceylonapz.babytube.BuildConfig
import com.ceylonapz.babytube.data.device.DeviceCountryRepository
import com.ceylonapz.babytube.data.local.DataStoreSearchCursorStore
import com.ceylonapz.babytube.data.local.SearchCursorStore
import com.ceylonapz.babytube.data.remote.VideoRemoteDataSource
import com.ceylonapz.babytube.data.remote.YouTubeRemoteDataSource
import com.ceylonapz.babytube.data.remote.createHttpClient
import com.ceylonapz.babytube.data.repository.VideoRepositoryImpl
import com.ceylonapz.babytube.domain.repository.CountryRepository
import com.ceylonapz.babytube.domain.repository.VideoRepository
import com.ceylonapz.babytube.domain.usecase.GetVideoRowsUseCase
import com.ceylonapz.babytube.domain.usecase.SearchVideosUseCase
import com.ceylonapz.babytube.presentation.home.HomeViewModel
import com.ceylonapz.babytube.presentation.search.SearchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val Context.searchCursorDataStore by preferencesDataStore(name = "search_cursors")

// The single place where data implementations are bound to domain contracts.
val dataModule = module {
    single<HttpClient> { createHttpClient(Android.create(), enableLogging = BuildConfig.DEBUG) }
    single<VideoRemoteDataSource> { YouTubeRemoteDataSource(get(), BuildConfig.YOUTUBE_API_KEY) }
    single<SearchCursorStore> { DataStoreSearchCursorStore(androidContext().searchCursorDataStore) }
    single<VideoRepository> { VideoRepositoryImpl(get(), get(), Dispatchers.IO) }
    single<CountryRepository> { DeviceCountryRepository(androidContext()) }
}

val domainModule = module {
    factory { GetVideoRowsUseCase(get(), get()) }
    factoryOf(::SearchVideosUseCase)
}

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
}

val appModules = listOf(dataModule, domainModule, presentationModule)
