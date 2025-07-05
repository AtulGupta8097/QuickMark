package com.example.groceryapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityAddressBinding;
import com.example.groceryapp.viewModels.UserViewModel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddressActivity extends AppCompatActivity {
    ActivityAddressBinding binding;

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final int GPS_REQUEST_CODE = 101;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<Intent> addressIntent;

    String userAddress = "";
    UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCAPxtkAKGleUU0bDITUYhCbAk28HdM5s8");
        }

        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Activity result launcher (fixed shadowing)
        addressIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent mapAddress = result.getData();
                        userAddress = mapAddress.getStringExtra("selected_address");
                        userViewModel.saveUserAddressInPref(userAddress);
                        userViewModel.saveUserAddressToFirebase(userAddress);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("selected_address", userAddress);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                });

        setupAutocompleteFragment();
        onBackBtnClicked();
        onGetCurrentLocationClicked();
    }

    private void setupAutocompleteFragment() {
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null) {
                        double lat = place.getLatLng().latitude;
                        double lng = place.getLatLng().longitude;

                        Intent mapAddressIntent = new Intent(AddressActivity.this, MapActivity.class);
                        mapAddressIntent.putExtra("lat", lat);
                        mapAddressIntent.putExtra("lng", lng);
                        addressIntent.launch(mapAddressIntent);
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(AddressActivity.this,
                            "Error selecting place: " + status.getStatusMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onGetCurrentLocationClicked() {
        binding.btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        binding.btnCurrentLocation.setText("Fetching your location");
        binding.gpsImage.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCurrentLocation.setTextColor(getResources().getColor(R.color.dark_grey, getTheme()));
        binding.gpsText.setTextColor(getResources().getColor(R.color.dark_grey, getTheme()));

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build();

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            fusedLocationClient.removeLocationUpdates(this);
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(AddressActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(
                                            location.getLatitude(),
                                            location.getLongitude(), 1);

                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        userAddress = address.getAddressLine(0);
                                        userViewModel.saveUserAddressInPref(userAddress);
                                        userViewModel.saveUserAddressToFirebase(userAddress);

                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("selected_address", userAddress);
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    } else {
                                        binding.gpsText.setText("No address found");
                                    }
                                } catch (IOException e) {
                                    binding.gpsText.setText(e.toString());
                                }
                            } else {
                                binding.gpsText.setText("Location is null");
                            }
                        }
                    }, getMainLooper());
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(AddressActivity.this, GPS_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onBackBtnClicked() {
        binding.backBtn.setOnClickListener(v -> {
           finish();
        });
    }

    // permission + gps resolution results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE && resultCode == RESULT_OK) {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
