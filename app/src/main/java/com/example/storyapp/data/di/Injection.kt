package com.example.storyapp.data.di

import android.content.Context
import com.example.storyapp.data.api.local.database.StoryDatabase
import com.example.storyapp.data.api.remote.retrofit.ApiConfig
import com.example.storyapp.data.preference.UserPreference
import com.example.storyapp.data.preference.dataStore
import com.example.storyapp.data.repository.UserRepository

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val database = StoryDatabase.getDatabase(context)
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(context)
        return UserRepository.getInstance(pref, apiService, database)
    }
}