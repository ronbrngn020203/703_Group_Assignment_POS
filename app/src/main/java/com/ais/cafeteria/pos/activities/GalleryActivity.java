package com.ais.cafeteria.pos.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.adapters.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView rvGallery = findViewById(R.id.rvGallery);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        List<GalleryAdapter.GalleryItem> galleryItems = new ArrayList<>();

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800",
                "AIS Main Building"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1567521464027-f127ff144326?w=800",
                "Cafeteria Interior"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=800",
                "Kitchen Team"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
                "Fresh Salad Bar"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800",
                "Coffee Station"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800",
                "Student Dining Area"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800",
                "Daily Specials"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1486427944299-d1955d23e34d?w=800",
                "Dessert Corner"));

        galleryItems.add(new GalleryAdapter.GalleryItem(
                "https://images.unsplash.com/photo-1505252585461-04db1eb84625?w=800",
                "Smoothie Bar"));

        GalleryAdapter adapter = new GalleryAdapter(galleryItems);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        rvGallery.setAdapter(adapter);
    }
}