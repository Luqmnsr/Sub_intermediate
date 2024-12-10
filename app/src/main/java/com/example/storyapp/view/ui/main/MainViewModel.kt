package com.example.storyapp.view.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.storyapp.data.api.remote.response.ListStoryItem
import com.example.storyapp.data.preference.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.results.Result
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getAllStories(): LiveData<Result<PagingData<ListStoryItem>>> {
        return repository.getAllStories()
    }

}
