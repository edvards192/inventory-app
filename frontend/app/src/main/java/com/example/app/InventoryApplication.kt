package com.example.app

import android.app.Application
import com.example.app.data.remote.RetrofitClient

class InventoryApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        RetrofitClient.initialize(this)
    }
}