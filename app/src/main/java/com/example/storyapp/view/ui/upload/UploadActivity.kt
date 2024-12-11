package com.example.storyapp.view.ui.upload

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.storyapp.R
import com.example.storyapp.data.results.Result
import com.example.storyapp.databinding.ActivityUploadBinding
import com.example.storyapp.view.factory.ViewModelFactory
import com.example.storyapp.view.ui.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UploadActivity : AppCompatActivity() {

    private val viewModel by viewModels<UploadViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityUploadBinding
    private var currentImageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat: Double? = null
    private var lon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupActions()
        setupBackPressedCallback()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.cbCurrentLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getCurrentLocation { latitude, longitude ->
                    lat = latitude
                    lon = longitude
                }
            } else {
                lat = null
                lon = null
            }
        }
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupActions() {
        binding.galleryButton.setOnClickListener {
            galleryLaunch.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.cameraButton.setOnClickListener {
            currentImageUri = getImageUri(this)
            cameraLaunch.launch(currentImageUri!!)
        }

        binding.uploadButton.setOnClickListener {
            handleUpload()
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackPressedDialog()
            }
        })
    }

    private fun handleUpload() {
        if (!binding.etDescription.text.isNullOrBlank() && currentImageUri != null) {
            currentImageUri?.let { uri ->
                val imageFile = uriToFile(uri, this).reduceFileImage()
                val desc = binding.etDescription.text.toString()

                showLoading(true)

                val requestBody = desc.toRequestBody("text/plain".toMediaType())
                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)
                val latRequest = lat?.toString()?.toRequestBody("text/plain".toMediaType())
                val lonRequest = lon?.toString()?.toRequestBody("text/plain".toMediaType())

                viewModel.uploadStory(multipartBody, requestBody, latRequest, lonRequest).observe(this) { response ->
                    when (response) {
                        is Result.Error -> showToast(getString(R.string.failed_upload))
                        Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showToast(getString(R.string.success_upload))
                            navigateToMainActivity()
                        }
                    }
                }
            }
        } else {
            showToast(getString(R.string.not_valid))
        }
    }

    private fun getCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationResult(it.latitude, it.longitude)
            } ?: showToast(getString(R.string.location_not_found))
        }
    }

    private fun showBackPressedDialog() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.upload_confirmation)
            setPositiveButton(R.string.done) { _, _ ->
                navigateToMainActivity()
            }
            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun navigateToMainActivity() {
        runBlocking {
            delay(500)
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun imageShow() {
        currentImageUri?.let { uri ->
            binding.ivStoryImage.setImageURI(uri)
            binding.ivStoryImage.background = null
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        showLoading(false)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToMainActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val galleryLaunch = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            currentImageUri = uri
            imageShow()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private val cameraLaunch = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            imageShow()
        } else {
            currentImageUri = null
        }
    }
}
