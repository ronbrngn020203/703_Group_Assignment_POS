package com.ais.cafeteria.pos.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ais.cafeteria.pos.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindUsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private static final double AIS_LAT = -36.8735583;
    private static final double AIS_LNG = 174.7208374;
    private static final String AIS_NAME = "AIS Cafeteria - Auckland Institute of Studies";
    private static final String AIS_ADDRESS = "28A Linwood Avenue, Mount Albert, Auckland 1025";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvGpsStatus;
    private TextView tvDistance;

    private LatLng aisLocation;
    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_us);

        aisLocation = new LatLng(AIS_LAT, AIS_LNG);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnOpenMaps = findViewById(R.id.btnOpenMaps);
        Button btnDirections = findViewById(R.id.btnDirections);
        Button btnMyLocation = findViewById(R.id.btnMyLocation);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        tvDistance = findViewById(R.id.tvDistance);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnOpenMaps.setOnClickListener(v -> openInMaps());
        btnDirections.setOnClickListener(v -> openDirections());
        btnMyLocation.setOnClickListener(v -> requestLocationAndFocus(true));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.addMarker(new MarkerOptions()
                .position(aisLocation)
                .title("AIS Cafeteria")
                .snippet(AIS_ADDRESS));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aisLocation, 17f));

        requestLocationAndFocus(false);
    }

    private void openInMaps() {
        Uri gmmIntentUri = Uri.parse("geo:" + AIS_LAT + "," + AIS_LNG + "?q=" + Uri.encode(AIS_NAME));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://maps.google.com/?q=" + Uri.encode(AIS_ADDRESS))));
        }
    }

    private void openDirections() {
        Uri navigationUri = Uri.parse("google.navigation:q=" + AIS_LAT + "," + AIS_LNG + "&mode=w");
        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigationUri);
        navigationIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(navigationIntent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                            + AIS_LAT + "," + AIS_LNG)));
        }
    }

    private void requestLocationAndFocus(boolean moveCameraToUser) {
        if (hasLocationPermission()) {
            enableMyLocation();
            fetchCurrentLocation(moveCameraToUser);
        } else {
            tvGpsStatus.setText("Allow location access to show your current position on the map.");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (googleMap != null && hasLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation(boolean moveCameraToUser) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                tvGpsStatus.setText("GPS is available, but the device has not reported a location yet.");
                tvDistance.setText("Distance unavailable");
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(aisLocation, 17f));
                }
                return;
            }

            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            updateMapForUser(moveCameraToUser);
            updateDistanceInfo(location);
        }).addOnFailureListener(e -> {
            tvGpsStatus.setText("Unable to read the current device location.");
            tvDistance.setText("Distance unavailable");
        });
    }

    private void updateMapForUser(boolean moveCameraToUser) {
        if (googleMap == null || userLocation == null) {
            return;
        }

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(aisLocation)
                .title("AIS Cafeteria")
                .snippet(AIS_ADDRESS));

        googleMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Your current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if (moveCameraToUser) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f));
            return;
        }

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(aisLocation)
                .include(userLocation)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
    }

    private void updateDistanceInfo(Location currentLocation) {
        float[] results = new float[1];
        Location.distanceBetween(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                AIS_LAT,
                AIS_LNG,
                results
        );

        float distanceKm = results[0] / 1000f;
        tvGpsStatus.setText("Your GPS location is active.");
        tvDistance.setText(String.format(java.util.Locale.getDefault(),
                "About %.2f km from AIS Cafeteria", distanceKm));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationAndFocus(false);
            } else {
                tvGpsStatus.setText("Location permission denied. You can still open AIS Cafeteria in Maps.");
                tvDistance.setText("Distance unavailable");
            }
        }
    }
}
