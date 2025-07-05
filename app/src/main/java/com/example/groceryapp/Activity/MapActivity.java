package com.example.groceryapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String userAddress = "";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private double searchedLat = 0.0;
    private double searchedLng = 0.0;
    private ActivityMapBinding binding;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent() != null) {
            searchedLat = getIntent().getDoubleExtra("lat", 0.0);
            searchedLng = getIntent().getDoubleExtra("lng", 0.0);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
        }

        onConfirmBtnClicked();
    }

    private void onConfirmBtnClicked() {
        binding.confirmBtn.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_address", userAddress);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng searchedLocation = new LatLng(searchedLat, searchedLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 18f));

        mMap.setMaxZoomPreference(20f);
        mMap.setMinZoomPreference(15f);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                        }
                    });
        } else {
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnCameraIdleListener(() -> {
            LatLng centerLatLng = mMap.getCameraPosition().target;
            showAddress(centerLatLng);
            showDistance(centerLatLng);
        });
    }

    private void showAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressObj = addresses.get(0);
                String featureName = addressObj.getFeatureName();
                String mainHeading = "";

                if (featureName != null && !featureName.matches(".*\\+.*") && !TextUtils.isDigitsOnly(featureName)) {
                    mainHeading = featureName;
                } else if (addressObj.getPremises() != null) {
                    mainHeading = addressObj.getPremises();
                } else if (addressObj.getSubLocality() != null) {
                    mainHeading = addressObj.getSubLocality();
                } else if (addressObj.getLocality() != null) {
                    mainHeading = addressObj.getLocality();
                } else if (addressObj.getAdminArea() != null) {
                    mainHeading = addressObj.getAdminArea();
                } else {
                    mainHeading = "Unnamed Place";
                }

                String subLocality = addressObj.getSubLocality();
                String locality = addressObj.getLocality();
                String subAdminArea = addressObj.getSubAdminArea();
                String adminArea = addressObj.getAdminArea();
                String postalCode = addressObj.getPostalCode();

                List<String> parts = new ArrayList<>();
                if (subLocality != null && !subLocality.equals(mainHeading)) parts.add(subLocality);
                if (locality != null && !locality.equals(mainHeading)) parts.add(locality);
                if (subAdminArea != null) parts.add(subAdminArea);
                if (adminArea != null) parts.add(adminArea);
                if (postalCode != null) parts.add(postalCode);

                String detailedAddress = TextUtils.join(", ", parts);

                // ✅ Update the userAddress variable
                userAddress = mainHeading + ", " + detailedAddress;

                String finalMainHeading = mainHeading;
                runOnUiThread(() -> {
                    binding.titleText.setText(finalMainHeading);
                    binding.addressText.setText(detailedAddress);
                });

            } else {
                runOnUiThread(() -> {
                    binding.titleText.setText("Location not found");
                    binding.addressText.setText("");
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                binding.titleText.setText("Error");
                binding.addressText.setText("Could not fetch address");
            });
        }
    }


    private void showDistance(LatLng targetLatLng) {
        if (currentLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    targetLatLng.latitude, targetLatLng.longitude,
                    results);
            float distanceInKm = results[0] / 1000f;

            runOnUiThread(() -> binding.distanceText.setText(
                    String.format(Locale.getDefault(),
                            "Distance: %.2f km away from current location", distanceInKm)));
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                currentLocation = location;
                                showDistance(targetLatLng);
                            } else {
                                runOnUiThread(() -> binding.distanceText.setText(""));
                            }
                        });
            } else {
                runOnUiThread(() -> binding.distanceText.setText(""));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);

                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(location -> {
                                    if (location != null) {
                                        currentLocation = location;
                                    }
                                });
                    }
                }
            } else {
                Toast.makeText(this,
                        "Location permission denied. Distance won’t be shown.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
