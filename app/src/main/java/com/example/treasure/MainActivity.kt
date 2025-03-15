/*
* Hunter Clarke
* OSU CS 492
* */

package com.example.treasure

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import com.google.android.gms.location.*
import androidx.core.content.ContextCompat
import android.location.Location
import android.os.Looper
import androidx.compose.ui.res.stringResource
import com.example.treasure.MainActivity.Companion.REQUEST_CHECK_SETTINGS
import com.google.android.gms.common.api.ResolvableApiException
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Log.d("DEBUG", "Location settings enabled, retrying...")
                    }
                    Activity.RESULT_CANCELED -> {
                        //Log.d("DEBUG", "Location settings not enabled")
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CHECK_SETTINGS = 1001
    }
}


@Composable
fun StartPage(navController: NavController, timerState: TimerState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.treasureHunt),
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val rulesText = """
        ${stringResource(R.string.rules_title)}
        
        ${stringResource(R.string.rule_1)}
        ${stringResource(R.string.rule_2)}
        ${stringResource(R.string.rule_3)}
        ${stringResource(R.string.rule_4)}
        ${stringResource(R.string.rule_5)}
    """.trimIndent()

        Text(
            text = rulesText,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(vertical = 16.dp)
        )

        Button(
            onClick = {
                timerState.startTimer()
                navController.navigate("cluePage")
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.startGame))
        }
    }
}

@Composable
fun ClueSolvedPage(navController: NavController, timerState: TimerState) {
    LaunchedEffect(Unit) {
        timerState.pauseTimer()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clue Solved!",
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.localBoyzDesc),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TimerDisplay(timerState)

        Button(
            onClick = {
                timerState.resumeTimer()
                navController.navigate("cluePage2")
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.cont))
        }
    }
}

@Composable
fun TreasureHuntCompletedPage(navController: NavController, timerState: TimerState) {
    LaunchedEffect(Unit) {
        timerState.stopTimer()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.complete),
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Total Elapsed Time: ${timerState.getElapsedTime() / 1000} seconds",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.reserDesc),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { navController.navigate("startPage") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.home))
        }
    }
}


@Composable
fun TimerDisplay(timerState: TimerState) {
    val elapsedTime by timerState.elapsedTime

    LaunchedEffect(timerState.isRunning) {
        while (timerState.isRunning) {
            timerState.elapsedTime.value = System.currentTimeMillis() - timerState.startTime
            delay(100)
        }
    }

    Text(
        text = "Elapsed Time: ${elapsedTime / 1000} seconds",
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@SuppressLint("MissingPermission")
fun fetchCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit,
    onError: (String) -> Unit
) {
    //Log.d("DEBUG", "Fetching current location using requestLocationUpdates()...")

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        //Log.d("DEBUG", "Location permissions not granted!")
        onError("Location permissions not granted!")
        return
    }

    val locationRequest = LocationRequest.create().apply {
        priority = Priority.PRIORITY_HIGH_ACCURACY
        interval = 10_000
        fastestInterval = 5_000
    }

    val locationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .build()

    val settingsClient = LocationServices.getSettingsClient(context)
    settingsClient.checkLocationSettings(locationSettingsRequest)
        .addOnSuccessListener {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    if (location != null) {
                        //Log.d("DEBUG", "Successfully retrieved location: ${location.latitude}, ${location.longitude}")
                        onLocationReceived(location)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    super.onLocationAvailability(locationAvailability)
                    if (!locationAvailability.isLocationAvailable) {
                        //Log.d("DEBUG", "Location is not available")
                        onError("Click Found It! to check location")
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            (context as Activity).startIntentSenderForResult(
                                exception.resolution.intentSender,
                                REQUEST_CHECK_SETTINGS,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            onError("Failed to start resolution activity")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        onError("Location settings cannot be changed")
                    }
                }
            } else {
                onError("Location settings check failed: ${exception.message}")
            }
        }
}

fun isNearLocation(x1: Double, y1: Double, x2: Double, y2: Double, buffer: Double): Boolean {
    return abs(x1 - x2) <= buffer && abs(y1 - y2) <= buffer
}




fun checkLocationSettings(context: Context, locationRequest: LocationRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val settingsClient = LocationServices.getSettingsClient(context)
    val locationSettingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .build()

    settingsClient.checkLocationSettings(locationSettingsRequest)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}