package com.example.storyapp.view.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.api.remote.response.DetailResponse
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.results.Result

class DetailViewModel(private val repository: UserRepository) : ViewModel() {

    fun getDetailStory(
        id: String
    ): LiveData<Result<DetailResponse>> {
        return repository.getDetailStory(id)
    }
}