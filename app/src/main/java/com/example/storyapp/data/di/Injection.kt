package com.example.storyapp.data.di

import android.content.Context
import com.example.storyapp.data.api.remote.retrofit.ApiConfig
import com.example.storyapp.data.preference.UserPreference
import com.example.storyapp.data.preference.dataStore
import com.example.storyapp.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        return UserRepository.getInstance(pref, apiService)
    }
}