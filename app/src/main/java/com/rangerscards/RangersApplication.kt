package com.rangerscards

import android.app.Application
import com.rangerscards.data.AppContainer
import com.rangerscards.data.AppDataContainer

class RangersApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}