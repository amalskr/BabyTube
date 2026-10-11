package com.ceylonapz.babytube.di

import com.ceylonapz.babytube.data.repository.VideoRepositoryImpl
import com.ceylonapz.babytube.data.source.SampleVideoDataSource
import com.ceylonapz.babytube.data.source.VideoDataSource
import com.ceylonapz.babytube.domain.repository.VideoRepository
import com.ceylonapz.babytube.domain.usecase.GetVideoUseCase
import com.ceylonapz.babytube.domain.usecase.GetVideosUseCase
import com.ceylonapz.babytube.presentation.home.HomeViewModel
import com.ceylonapz.babytube.presentation.player.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// The single place where data implementations are bound to domain contracts.
val dataModule = module {
    single<VideoDataSource> { SampleVideoDataSource() }
    single<VideoRepository> { VideoRepositoryImpl(get(), Dispatchers.IO) }
}

val domainModule = module {
    factoryOf(::GetVideosUseCase)
    factoryOf(::GetVideoUseCase)
}

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    // videoId comes from parametersOf(...) at the call site.
    viewModelOf(::PlayerViewModel)
}

val appModules = listOf(dataModule, domainModule, presentationModule)
