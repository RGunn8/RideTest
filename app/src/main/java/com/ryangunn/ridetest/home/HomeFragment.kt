package com.ryangunn.ridetest.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ryangunn.ridetest.LocationService
import com.ryangunn.ridetest.LocationService.Companion.currentLocation
import com.ryangunn.ridetest.LocationService.Companion.distanceTravel
import com.ryangunn.ridetest.LocationService.Companion.isTracking
import com.ryangunn.ridetest.LocationService.Companion.pathPoints
import com.ryangunn.ridetest.LocationService.Companion.startLocation
import com.ryangunn.ridetest.LocationService.Companion.timeInStopWatchTime
import com.ryangunn.ridetest.LocationService.Companion.timeRunInMillis
import com.ryangunn.ridetest.R
import com.ryangunn.ridetest.database.model.Moves
import com.ryangunn.ridetest.databinding.FragmentHomeBinding
import com.ryangunn.ridetest.util.Constants
import com.ryangunn.ridetest.util.Extensions.hasLocationPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*

@ExperimentalCoroutinesApi
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var map: GoogleMap? = null

    private val currentPath = mutableListOf<LatLng>()

    lateinit var homeViewModel: HomeViewModel

    private var currentDistance = 0.0

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
            startLocationService(mapFragment)
        } else {
            //No permission to use location
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        if (!requireContext().hasLocationPermission()) {
            showLocationPermissionRationaleDialog()
        } else {
            startLocationService(mapFragment)
        }
    }

    private fun showLocationPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.location_permission))
            .setMessage(getString(R.string.location_permission_detail))
            .setPositiveButton(
                getString(R.string.okay)
            ) { dialog, _ ->
                run {
                    locationPermissionRequest.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                    dialog.dismiss()
                }
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService(mapFragment: SupportMapFragment) {
        sendCommand(Constants.ACTION_INITIAL)
        binding.apply {
            mapFragment.getMapAsync { asyncMap ->
                map = asyncMap
                if (requireContext().hasLocationPermission()) {
                    map?.isMyLocationEnabled = true
                }
                map?.setOnMapClickListener { latLon ->
                    onMapItemClick(latLon)
                }
                collectLocationServiceFlows()
            }
        }
    }

    private fun onMapItemClick(it: LatLng) {
        map?.clear()
        map?.addMarker(MarkerOptions().position(it))
        val circle = CircleOptions().center(it).radius(Constants.RADIUS).visible(false)
        LocationService.destination.value = circle
        map?.addCircle(circle)
        showStartButtons()
    }

    private fun showStartButtons() {
        binding.apply {
            startButton.visibility = View.VISIBLE
            clearButton.visibility = View.VISIBLE
            timeTextView.visibility = View.VISIBLE
            startButton.setOnClickListener {
                onStartButtonClick()
            }
            clearButton.setOnClickListener {
                clear()
            }
        }
    }

    private fun onStartButtonClick() {
        currentPath.clear()
        sendCommand(Constants.ACTION_START_OR_RESUME)
        distanceTravel.value = 0f
        timeRunInMillis.value = 0
        currentDistance = 0.0
        currentLocation.value?.let { currentLocation ->
            startLocation.value =
                LatLng(currentLocation.latitude, currentLocation.longitude)
        }
        map?.setOnMapClickListener { }
        hideStartButtons()
        showFinishButtons()
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (latLon in currentPath) {
            bounds.include(latLon)
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun showFinishButtons() {
        binding.apply {
            finishButton.visibility = View.VISIBLE
            finishButton.setOnClickListener {
                finishMove()
            }
        }
    }

    private fun finishMove() {
        binding.apply {
            sendCommand(Constants.ACTION_STOP_SERVICE)
            map?.setOnMapClickListener {
                onMapItemClick(it)
            }
            hideFinishButton()
            zoomToSeeWholeTrack()
            map?.snapshot {
                val move = Moves(
                    timeTextView.text.toString(),
                    currentDistance,
                    it,
                    Calendar.getInstance().timeInMillis
                )
                homeViewModel.insertMove(move, requireContext())
                sendCommand(Constants.ACTION_INITIAL)
                map?.clear()
            }
        }
    }

    private fun hideStartButtons() {
        binding.apply {
            startButton.visibility = View.GONE
            clearButton.visibility = View.GONE
        }
    }

    private fun hideFinishButton() {
        binding.apply {
            finishButton.visibility = View.GONE
        }
    }

    private fun clear() {
        map?.clear()
        hideStartButtons()
        timeInStopWatchTime.value = "00:00:00"
        distanceTravel.value = 0f
        currentPath.clear()
    }

    private fun collectLocationServiceFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    currentLocation.collect { location ->
                        location?.let {
                            val position = CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude),
                                15f
                            )
                            map?.animateCamera(position)
                        }
                    }
                }

                launch {
                    homeViewModel.newMoveIdFlow.collect {
                        findNavController().navigate(HomeFragmentDirections.showFinishDialog(it))
                    }
                }

                launch {
                    pathPoints.collect {
                        if (isTracking.value) {
                            val position = CameraUpdateFactory.newLatLngZoom(
                                it,
                                15f
                            )
                            map?.animateCamera(position)
                            addLatestPolyline(it)
                        }
                    }
                }

                launch {
                    timeInStopWatchTime.collect {
                        binding.timeTextView.text = it
                    }
                }

                launch {
                    distanceTravel.collect {
                        val miles = it.toDouble() * 0.621371
                        currentDistance = miles
                        val df = DecimalFormat("#.00")
                        val mileString = df.format(miles)
                        binding.distanceTextView.text = "Miles: $mileString"
                    }
                }

                launch {
                    LocationService.finishedRide.collect {
                        if (it) {
                            finishMove()
                        }
                    }
                }

            }
        }

    }


    private fun addLatestPolyline(latLng: LatLng) {
        currentPath.add(latLng)
        if (pathPoints.replayCache.isNotEmpty()) {
            val preLastLatLng = pathPoints.replayCache[0]
            val polylineOptions = PolylineOptions()
                .color(R.color.purple_200)
                .width(10f)
                .add(preLastLatLng)
                .add(latLng)
            map?.addPolyline(polylineOptions)
        }
    }


    private fun sendCommand(action: String) {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = action
            context?.startService(it)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}