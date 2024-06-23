package com.example.toilet_search

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ToiletSearchScreen() {
    GoogleMapsScreen()
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    GoogleMapsScreen()
}

/** 東京駅 */
val initPos = LatLng(35.6809591, 139.7673068)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapsScreen() {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    var location by remember { mutableStateOf<LatLng?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initPos, 10f)
    }
    RequestLocationPermission { granted ->
        permissionGranted = granted
    }
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            locationHelper.getLastLocation { loc ->
                if (loc != null) {
                    location = LatLng(loc.latitude, loc.longitude)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { contentPadding ->
            GoogleMap(
                modifier = Modifier.padding(contentPadding),
                cameraPositionState = cameraPositionState,
            ) {
                Marker(
                    state = MarkerState(position = initPos),
                    title = "東京駅です"
                )
            }
        },
    )
}

class LocationHelper(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocationReceived: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                onLocationReceived(location)
            }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    if (permissionState.status.isGranted) onPermissionResult(true)
    else onPermissionResult(false)
}