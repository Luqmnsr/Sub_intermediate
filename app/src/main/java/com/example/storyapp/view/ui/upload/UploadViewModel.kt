package com.example.storyapp.view.ui.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.api.remote.response.ErrorResponse
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.results.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadViewModel(private val repository: UserRepository) : ViewModel() {

    fun uploadStory(
        file: MultipartBody.Part,
        description: RequestBody,
        lat : RequestBody? = null,
        lon : RequestBody? = null
    ): LiveData<Result<ErrorResponse>> {
        return repository.uploadStory(file, description,lat,lon)
    }
}