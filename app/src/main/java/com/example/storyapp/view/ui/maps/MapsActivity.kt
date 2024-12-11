package com.example.storyapp.view.ui.maps

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.storyapp.data.results.Result
import com.example.storyapp.R
import com.example.storyapp.data.api.remote.response.ListStoryItem
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.view.factory.ViewModelFactory
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getMyLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configureMapSettings()
        getMyLocation()
        setMapStyle()
        observeStories()
    }

    private fun configureMapSettings() {
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun observeStories(location: Int = 1) {
        viewModel.getStoriesWithLocation(location).observe(this) { result ->
            when (result) {
                is Result.Loading -> Log.i(TAG, "Loading data...")
                is Result.Success -> addMarkersToMap(result.data)
                is Result.Error -> toast(result.error)
            }
        }
    }

    private fun addMarkersToMap(storyData: List<ListStoryItem>) {
        val boundsBuilder = LatLngBounds.Builder()
        storyData.forEach { item ->
            val latLng = LatLng(
                (item.lat as? Number)?.toDouble() ?: 0.0,
                (item.lon as? Number)?.toDouble() ?: 0.0
            )
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(item.name)
                    .snippet(item.description)
                    .icon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(R.drawable.ic_story_auto)))
            )
            boundsBuilder.include(latLng)
        }
        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
    }

    private fun vectorToBitmap(drawableId: Int): Bitmap {
        val drawable = ResourcesCompat.getDrawable(resources, drawableId, null)
            ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}
