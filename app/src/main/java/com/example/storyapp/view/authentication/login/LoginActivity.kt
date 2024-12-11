package com.example.storyapp.view.authentication.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.storyapp.R
import com.example.storyapp.data.preference.UserModel
import com.example.storyapp.data.results.Result
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.view.factory.ViewModelFactory
import com.example.storyapp.view.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_field_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)

            loginViewModel.login(email, password).observe(this) { loginResponse ->
                showLoading(false)
                when (loginResponse) {
                    is Result.Success -> {
                        val loginResult = loginResponse.data.loginResult
                        showDialog(true, loginResult.name)
                        val userModel = UserModel(
                            email = email,
                            token = loginResult.token,
                            isLogin = true
                        )
                        loginViewModel.saveSession(userModel)
                    }
                    is Result.Error -> {
                        showDialog(false, email)
                    }
                    is Result.Loading -> {
                        showLoading(true)
                    }
                }
            }
        }
    }

    private fun showDialog(isSuccess: Boolean, name: String) {
        showLoading(false)
        val dialogBuilder = AlertDialog.Builder(this)

        if (isSuccess) {
            // Dialog Success
            dialogBuilder.apply {
                setTitle(getString(R.string.login_success_title))
                setMessage(getString(R.string.login_success_message, name))
                setPositiveButton(getString(R.string.login_success_positive_button)) { _, _ ->
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            // Dialog Error
            dialogBuilder.apply {
                setTitle(getString(R.string.login_error_title))
                setMessage(getString(R.string.login_error_message_email_used, name))
                setPositiveButton(getString(R.string.login_error_positive_button)) { dialog, _ ->
                    dialog.dismiss()
                }
            }
        }
        //Show Dialog
        dialogBuilder.create().show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                login
            )
            startDelay = 100
        }.start()
    }

}