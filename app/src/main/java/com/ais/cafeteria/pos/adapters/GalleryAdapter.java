package com.ais.cafeteria.pos.adapters;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ais.cafeteria.pos.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    public static class GalleryItem {
        public final String imageUrl;
        public final String label;

        public GalleryItem(String imageUrl, String label) {
            this.imageUrl = imageUrl;
            this.label = label;
        }
    }

    private final List<GalleryItem> items;

    public GalleryAdapter(List<GalleryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryItem item = items.get(position);
        holder.tvLabel.setText(item.label);

        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_gallery)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> showPopup(v, item));
    }

    private void showPopup(View anchor, GalleryItem item) {
        Dialog dialog = new Dialog(anchor.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_gallery_item);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        ImageView ivPopupImage = dialog.findViewById(R.id.ivPopupImage);
        TextView tvPopupLabel  = dialog.findViewById(R.id.tvPopupLabel);
        TextView tvClose       = dialog.findViewById(R.id.tvClose);

        Glide.with(anchor.getContext())
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_gallery)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivPopupImage);

        tvPopupLabel.setText(item.label);
        tvClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvLabel;

        GalleryViewHolder(View v) {
            super(v);
            ivThumbnail = v.findViewById(R.id.ivThumbnail);
            tvLabel     = v.findViewById(R.id.tvLabel);
        }
    }
}