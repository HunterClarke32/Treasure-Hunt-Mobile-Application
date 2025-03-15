/*
* Hunter Clarke
* OSU CS 492
* */

package com.example.treasure

import android.Manifest
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*

@Composable
fun CluePage(navController: NavController, timerState: TimerState) {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                //Log.d("DEBUG", "Permissions granted, attempting to fetch location...")
                isLoading = true
                fetchCurrentLocation(
                    context,
                    fusedLocationClient,
                    onLocationReceived = { location ->
                        isLoading = false
                        userLocation = location
                        //Log.d("DEBUG", "User Location Retrieved: ${location.latitude}, ${location.longitude}")
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                        //Log.d("DEBUG", "Error fetching location: $error")
                    }
                )
            } else {
                 //("DEBUG", "Location permissions denied")
                errorMessage = "Location permissions are required to play the game."
            }
        }
    )

    // Request permissions when this Composable is launched
    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.clue1),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        var showHint by remember { mutableStateOf(false) }

        Button(
            onClick = { showHint = true },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(stringResource(R.string.showHint))
        }

        // The hint is only displayed if the button is pressed
        if (showHint) {
            Text(
                text = stringResource(R.string.hint1),
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        TimerDisplay(timerState)

        Button(
            onClick = {
                //("DEBUG", "Location not available yet, requesting fresh location...")
                isLoading = true
                fetchCurrentLocation(
                    context,
                    fusedLocationClient,
                    onLocationReceived = { location ->
                        isLoading = false
                        userLocation = location
                        //Log.d("DEBUG", "Updated Location: ${location.latitude}, ${location.longitude}")

                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                        //Log.d("DEBUG", "Error fetching location: $error")
                    }
                )
                if(userLocation!!.latitude != null) {
                    // Perform the proximity check with the existing location
                    if (isNearLocation(
                            userLocation!!.latitude, userLocation!!.longitude,
                            44.567505, -123.272435, .005
                        )
                    ) {
                        //Log.d("DEBUG", "User is within range! Navigating to clueSolvedPage")
                        navController.navigate("clueSolvedPage")
                    } else {
                        //Log.d("DEBUG", "User is too far away!")
                        //Log.d(
                           // "DEBUG",
                            //"Updated Location: ${userLocation!!.latitude}, ${userLocation!!.longitude}" )
                        errorMessage = "You're not close enough to the target location."
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(R.string.foundButton))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        Button(
            onClick = {
                timerState.stopTimer()
                navController.navigate("startPage")
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(stringResource(R.string.quit))
        }
    }
}