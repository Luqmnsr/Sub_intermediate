package com.example.storyapp.data.repository

import com.example.storyapp.data.api.remote.retrofit.ApiService
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.storyapp.data.api.remote.response.DetailResponse
import com.example.storyapp.data.api.remote.response.ErrorResponse
import com.example.storyapp.data.api.remote.response.ListStoryItem
import com.example.storyapp.data.api.remote.response.LoginResponse
import com.example.storyapp.data.api.remote.response.RegisterResponse
import com.example.storyapp.data.preference.UserModel
import com.example.storyapp.data.preference.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import com.example.storyapp.data.results.Result
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun saveSession(user: UserModel) {
        return userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.register(name, email, password)
            emit(Result.Success(response))
        } catch (e: IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        } catch (e: Exception) {
            emit(Result.Error("Something went wrong: ${e.message}"))
        }
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.login(email, password)
            emit(Result.Success(response))
        } catch (e: IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        } catch (e: Exception) {
            emit(Result.Error("Something went wrong: ${e.message}"))
        }
    }

    fun getAllStories(
    ): LiveData<Result<List<ListStoryItem>>> = liveData {
        emit(Result.Loading)

        val token = userPreference.getSession().firstOrNull()?.token
        if (token.isNullOrEmpty()) {
            emit(Result.Error("Invalid session. Please login again."))
            return@liveData
        }

        try {
            val response = apiService.getAllStories().listStory
            emit(Result.Success(response))
        } catch (e: IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        } catch (e: Exception) {
            emit(Result.Error("Something went wrong: ${e.message}"))
        }
    }


    fun getDetailStory(
        id : String
    ): LiveData<Result<DetailResponse>> = liveData {
        emit(Result.Loading)

        try{
            val response = apiService.getDetailStory(id)
            emit(Result.Success(response))
        } catch (e: IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, DetailResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage))
        } catch (e: Exception) {
            emit(Result.Error("Something went wrong: ${e.message}"))
        }
    }

    fun uploadStory(
        file: MultipartBody.Part, description: RequestBody
    ): LiveData<Result<ErrorResponse>> = liveData {
        emit(Result.Loading)
        try{
            val response = apiService.uploadStory(file,description)
            emit(Result.Success(response))
        }catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        } catch (e: Exception) {
            emit(Result.Error("Something went wrong: ${e.message}"))
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(
                    userPreference,
                    apiService
                )
            }.also { instance = it }
    }
}