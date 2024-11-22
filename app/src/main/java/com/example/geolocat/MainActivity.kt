package com.example.geolocat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.geolocat.ui.theme.GeoloCATTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            GeoloCATTheme {
                var latitude by remember { mutableStateOf<String?>(null) }
                var longitude by remember { mutableStateOf<String?>(null) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Check permissions and request if necessary
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        getCurrentLocation(
                            onSuccess = { lat, lon ->
                                latitude = lat.toString()
                                longitude = lon.toString()
                            },
                            onError = { error ->
                                errorMessage = error
                            }
                        )
                    } else {
                        errorMessage = "Permission not granted"
                    }
                }

                LaunchedEffect(Unit) {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        getCurrentLocation(
                            onSuccess = { lat, lon ->
                                latitude = lat.toString()
                                longitude = lon.toString()
                            },
                            onError = { error ->
                                errorMessage = error
                            }
                        )
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }

                // UI
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            errorMessage != null -> Text(text = errorMessage!!)
                            latitude != null && longitude != null -> {
                                Text(text = "Широта: $latitude")
                                Text(text = "Долгота: $longitude")
                            }
                            else -> Text(text = "Получение координат...")
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    onError("Не удалось получить местоположение.")
                }
            }.addOnFailureListener {
                onError(it.message ?: "Ошибка при получении местоположения.")
            }
        } else {
            onError("Нет разрешения на доступ к местоположению.")
        }
    }
}
