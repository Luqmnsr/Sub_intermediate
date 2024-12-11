package com.example.storyapp.view.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.example.storyapp.view.adapter.StoryAdapter
import com.example.storyapp.view.factory.ViewModelFactory
import com.example.storyapp.view.adapter.LoadingStateAdapter
import com.example.storyapp.view.authentication.welcome.WelcomeActivity
import com.example.storyapp.view.ui.detail.DetailActivity
import com.example.storyapp.view.ui.maps.MapsActivity
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
            viewModel.getAllStories()
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
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
            story.id.let {
                intent.putExtra("EXTRA_STORY_ID", it)
                startActivity(intent)
            }
        }
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter.withLoadStateHeaderAndFooter(
                header = LoadingStateAdapter { storyAdapter.retry() },
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
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
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyMessage.visibility = View.GONE

                    storyAdapter.submitData(lifecycle, result.data)
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = getString(R.string.failed_to_load_stories)
                    Toast.makeText(this, "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}