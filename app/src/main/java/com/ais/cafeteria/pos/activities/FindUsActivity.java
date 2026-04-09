package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindUsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double AIS_LAT = -36.8819;
    private static final double AIS_LNG = 174.7120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_us);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnOpenMaps = findViewById(R.id.btnOpenMaps);

        btnBack.setOnClickListener(v -> onBackPressed());

        btnOpenMaps.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + AIS_LAT + "," + AIS_LNG +
                    "?q=" + Uri.encode("Auckland Institute of Studies, 28A Linwood Avenue, Mount Albert, Auckland 1025"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=28A+Linwood+Avenue,+Mount+Albert,+Auckland+1025")));
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng aisLocation = new LatLng(AIS_LAT, AIS_LNG);
        googleMap.addMarker(new MarkerOptions()
                .position(aisLocation)
                .title("AIS Cafeteria")
                .snippet("28A Linwood Avenue, Mount Albert, Auckland 1025"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aisLocation, 15f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}