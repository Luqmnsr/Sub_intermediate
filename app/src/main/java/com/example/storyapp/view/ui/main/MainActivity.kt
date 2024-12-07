package com.example.storyapp.view.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.data.results.Result
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.view.StoryAdapter
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.authentication.welcome.WelcomeActivity
import com.example.storyapp.view.ui.detail.DetailActivity
import com.example.storyapp.view.ui.upload.UploadActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        setupRecyclerViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        val isLoggedIn = viewModel.getSession().value?.isLogin ?: false
        if (isLoggedIn) {
            viewModel.getAllStories()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        viewModel.logout()
                        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                alertDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupAction() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
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
        supportActionBar?.show()
    }

    private fun setupRecyclerViews() {
        storyAdapter = StoryAdapter { story ->
            val intent = Intent(this, DetailActivity::class.java)
            story.id?.let {
                intent.putExtra("EXTRA_STORY_ID", it)
                startActivity(intent)
            } ?: Toast.makeText(this, "Story ID is missing", Toast.LENGTH_SHORT).show()
        }
        binding.rvStories.apply {
            adapter = storyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        viewModel.getAllStories().observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmptyMessage.visibility = View.GONE
                    Log.d("MainActivity", "Loading stories...")
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val stories = result.data
                    if (stories.isEmpty()) {
                        // Show empty message if no stories
                        binding.tvEmptyMessage.visibility = View.VISIBLE
                        binding.tvEmptyMessage.text = getString(R.string.unavailable_story)  // "No stories available"
                        Log.d("MainActivity", "No stories available.")
                    } else {
                        binding.tvEmptyMessage.visibility = View.GONE
                        storyAdapter.submitList(stories)
                        Log.d("MainActivity", "Stories loaded: ${stories.size}")
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = getString(R.string.failed_to_load_stories)  // Error loading stories

                    val errorMessage = result.error
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Error loading stories: $errorMessage")
                }
            }
        }
    }
}