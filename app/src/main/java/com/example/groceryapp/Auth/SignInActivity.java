package com.example.groceryapp.Auth;
import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.groceryapp.Activity.MapActivity;
import com.example.groceryapp.R;
import com.google.android.gms.common.api.ResolvableApiException;
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
import java.util.Objects;

public class SignInActivity extends AppCompatActivity{
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final int GPS_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView addressTextView;
    private Button currentLocationBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addressTextView = findViewById(R.id.address_text);
        currentLocationBtn = findViewById(R.id.btn_current_location);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCAPxtkAKGleUU0bDITUYhCbAk28HdM5s8", Locale.US);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup Autocomplete fragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("Search a new address");

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Intent intent = new Intent(SignInActivity.this, MapActivity.class);
                    intent.putExtra("lat", Objects.requireNonNull(place.getLatLng()).latitude);
                    intent.putExtra("lng", place.getLatLng().longitude);
                    startActivity(intent);
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    addressTextView.setText("Error: " + status.getStatusMessage());
                }
            });
        }

        currentLocationBtn.setOnClickListener(v -> getCurrentLocation());
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        // Use modern LocationRequest.Builder
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
                                Geocoder geocoder = new Geocoder(SignInActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    assert addresses != null;
                                    if (!addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        String fullAddress = address.getAddressLine(0);
                                        addressTextView.setText(fullAddress);
                                    } else {
                                        addressTextView.setText("No address found");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    addressTextView.setText("Error getting address");
                                }
                            } else {
                                addressTextView.setText("Location is null");
                            }
                        }
                    }, getMainLooper());
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(SignInActivity.this, GPS_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
                    }
                });
    }


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