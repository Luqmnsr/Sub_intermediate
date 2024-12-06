package com.example.storyapp.view.authentication.register

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.UserRepository

class RegisterViewModel (
    private val repository: UserRepository) : ViewModel() {
    fun register(
        name: String,
        email: String,
        password: String) = repository.register(name, email, password)
}