package com.example.myeduapp

import android.app.Application
import android.content.Context

class MyEduApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
