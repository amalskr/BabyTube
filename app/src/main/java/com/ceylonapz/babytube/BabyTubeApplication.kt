package com.ceylonapz.babytube

import android.app.Application
import com.ceylonapz.babytube.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BabyTubeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BabyTubeApplication)
            modules(appModules)
        }
    }
}
