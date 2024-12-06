package com.example.storyapp.view.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.api.remote.response.Story
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.data.results.Result
import com.example.storyapp.view.ViewModelFactory

class DetailActivity : AppCompatActivity() {

    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivStoryImage.transitionName = "shared_image"
        binding.tvStoryName.transitionName = "shared_name"
        binding.tvDescription.transitionName = "shared_description"

        setupActionBar()
        handleIntent()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun handleIntent() {
        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            getDetailStory(storyId)
        } else {
            Toast.makeText(this, getString(R.string.story_id_not_found), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getDetailStory(id: String) {
        viewModel.getDetailStory(id).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    populateStoryDetail(result.data.story)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateStoryDetail(story: Story) {
        binding.apply {
            tvStoryName.text = story.name
            tvDescription.text = story.description
            supportActionBar?.title = story.name

            Glide.with(binding.root.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.error_placeholder)
                .into(ivStoryImage)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STORY_ID = "EXTRA_STORY_ID"
    }
}
