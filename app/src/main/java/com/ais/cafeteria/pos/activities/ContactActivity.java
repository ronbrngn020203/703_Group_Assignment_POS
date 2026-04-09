package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;

public class ContactActivity extends AppCompatActivity {

    private static final String AIS_PHONE   = "+6498153589";
    private static final String AIS_EMAIL   = "cafeteria@ais.ac.nz";
    private static final String AIS_WEBSITE = "https://www.ais.ac.nz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ImageView btnBack    = findViewById(R.id.btnBack);
        Button btnDial       = findViewById(R.id.btnDial);
        Button btnEmail      = findViewById(R.id.btnEmail);
        Button btnWebsite    = findViewById(R.id.btnWebsite);

        btnBack.setOnClickListener(v -> onBackPressed());

        btnDial.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + AIS_PHONE));
            startActivity(dialIntent);
        });

        btnEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + AIS_EMAIL));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "AIS Cafeteria Enquiry");
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        btnWebsite.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AIS_WEBSITE));
            startActivity(browserIntent);
        });
    }
}