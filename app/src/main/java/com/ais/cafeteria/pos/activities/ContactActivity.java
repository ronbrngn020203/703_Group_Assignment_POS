package com.ais.cafeteria.pos.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ais.cafeteria.pos.R;

import java.util.Calendar;

public class ContactActivity extends AppCompatActivity {

    private static final String MANNA_PHONE   = "+64210101 2423";
    private static final String MANNA_EMAIL   = "cafeteria@ais.ac.nz";
    private static final String MANNA_WEBSITE = "https://www.ais.ac.nz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ImageView btnBack   = findViewById(R.id.btnBack);
        Button btnDial      = findViewById(R.id.btnDial);
        Button btnEmail     = findViewById(R.id.btnEmail);
        Button btnWebsite   = findViewById(R.id.btnWebsite);
        Button btnCalendar  = findViewById(R.id.btnCalendar);

        btnBack.setOnClickListener(v -> onBackPressed());

        // ── Dial Phone ────────────────────────────────────────
        btnDial.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + MANNA_PHONE));
            startActivity(dialIntent);
        });

        // ── Send Email ────────────────────────────────────────
        btnEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + MANNA_EMAIL));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Manna Cafe Enquiry");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi Manna Cafe & Catering,\n\n");
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // ── Browse Website ────────────────────────────────────
        btnWebsite.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MANNA_WEBSITE));
            startActivity(browserIntent);
        });

        // ── Google Calendar ───────────────────────────────────
        btnCalendar.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 7);
            cal.set(Calendar.MINUTE, 30);
            cal.set(Calendar.SECOND, 0);
            long startTime = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 16);
            cal.set(Calendar.MINUTE, 0);
            long endTime = cal.getTimeInMillis();

            Intent calIntent = new Intent(Intent.ACTION_INSERT);
            calIntent.setData(CalendarContract.Events.CONTENT_URI);
            calIntent.putExtra(CalendarContract.Events.TITLE, "Manna Cafe & Catering — Open");
            calIntent.putExtra(CalendarContract.Events.DESCRIPTION,
                    "Mon-Fri: 7:30 AM - 4:00 PM\nSaturday: 8:00 AM - 2:00 PM\nSunday: Closed");
            calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION,
                    "28A Linwood Avenue, Mount Albert, Auckland 1025");
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
            calIntent.putExtra(CalendarContract.Events.ALL_DAY, false);
            calIntent.putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
            try {
                startActivity(calIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No Calendar app found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}