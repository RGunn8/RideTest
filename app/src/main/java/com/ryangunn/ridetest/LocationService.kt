package com.ryangunn.ridetest

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.ryangunn.ridetest.util.Constants
import com.ryangunn.ridetest.util.Constants.FASTEST_LOCATION_INTERVAL
import com.ryangunn.ridetest.util.Constants.LOCATION_UPDATE_INTERVAL
import com.ryangunn.ridetest.util.Constants.NOTIFICATION_CHANNEL_ID
import com.ryangunn.ridetest.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.ryangunn.ridetest.util.Constants.NOTIFICATION_ID
import com.ryangunn.ridetest.util.Extensions.hasLocationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class LocationService : LifecycleService() {
    var serviceKilled = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableStateFlow(0L)

    companion object {
        val timeRunInMillis = MutableStateFlow(0L)
        val isTracking = MutableStateFlow(false)
        val startLocation = MutableStateFlow(LatLng(0.0, 0.0))
        val destination = MutableStateFlow(CircleOptions())
        val pathPoints: MutableSharedFlow<LatLng> = MutableSharedFlow(2)
        val currentLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
        val timeInStopWatchTime = MutableStateFlow("")
        val finishedRide = MutableStateFlow(false)
        val distanceTravel = MutableStateFlow(0f)
    }


    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun killService() {
        serviceKilled = true
        isTracking.value = false
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_INITIAL -> {
                    updateLocationTracking()
                }
                Constants.ACTION_START_OR_RESUME -> {
                    isTracking.value = true
                    startForegroundService()
                    startTimer()
                    timeStarted = System.currentTimeMillis()
                }
                Constants.ACTION_STOP_SERVICE -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value) {
                // time difference between now and timeStarted
                timeRun = System.currentTimeMillis() - timeStarted
                timeRunInMillis.value = timeRun
                timeInStopWatchTime.value = getFormattedStopWatchTime(timeRun, false)
                if (timeRunInMillis.value >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.value = timeRunInSeconds.value + 1
                    lastSecondTimestamp += 1000L
                }
                delay(500)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking() {
        if (this.hasLocationPermission()) {
            val request = LocationRequest.create().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_LOCATION_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener {
                    currentLocation.value = it
                }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Log.d(
                            "Test",
                            "NEW LOCATION: " + location.latitude + ", " + location.longitude
                        )
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        lifecycleScope.launch {
            location?.let {
                val pos = LatLng(location.latitude, location.longitude)
                val distance = FloatArray(2)
                val destinationCenterLatLong = destination.value.center
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    destinationCenterLatLong!!.latitude,
                    destinationCenterLatLong.longitude,
                    distance
                )
                if (pathPoints.replayCache.isNotEmpty()) {
                    val lastPoint = pathPoints.replayCache[0]
                    val currentDistanceTravel = distanceTravel.value
                    distanceTravel.value = currentDistanceTravel + distance(
                        lastPoint.latitude,
                        lastPoint.longitude,
                        location.latitude,
                        location.longitude
                    ).toFloat()
                }
                pathPoints.emit(pos)

                checkIfUserReachDestination(distance)
            }
        }
    }

    private fun checkIfUserReachDestination(distance: FloatArray) {
        if (distance[0] <= destination.value.radius) {
            killService()
            finishedRide.value = true
            isTracking.value = false
        }
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (sin(deg2rad(lat1))
                * sin(deg2rad(lat2))
                + (cos(deg2rad(lat1))
                * cos(deg2rad(lat2))
                * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.value = true


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Ride Test App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        CoroutineScope(Dispatchers.Main).launch {
            timeRunInSeconds.collect {
                if (!serviceKilled) {
                    val notification = notificationBuilder
                        .setContentText(getFormattedStopWatchTime(it * 1000L))
                    notificationManager.notify(NOTIFICATION_ID, notification.build())
                }
            }
        }

    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        FLAG_UPDATE_CURRENT
    )

    private fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if (!includeMillis) {
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}