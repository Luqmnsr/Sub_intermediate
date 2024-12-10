package com.example.storyapp.view.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.api.remote.response.ListStoryItem
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.results.Result

class MapsViewModel(private val repository: UserRepository) : ViewModel() {

    fun getStoriesWithLocation(
        location : Int = 1
    ): LiveData<Result<List<ListStoryItem>>> {
        return repository.getStoriesWithLocation(location)
    }
}