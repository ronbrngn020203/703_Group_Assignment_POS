package com.ais.cafeteria.pos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.models.CartItem;

import java.util.List;
import java.util.Locale;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

    private List<CartItem> items;

    public ReceiptAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Simple row layout created programmatically
        android.widget.LinearLayout row = new android.widget.LinearLayout(parent.getContext());
        row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        row.setPadding(0, 4, 0, 4);
        row.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));
        return new ReceiptViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        android.widget.LinearLayout row;

        ReceiptViewHolder(View itemView) {
            super(itemView);
            row = (android.widget.LinearLayout) itemView;
        }

        void bind(CartItem item) {
            row.removeAllViews();

            TextView tvItem = new TextView(row.getContext());
            tvItem.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvItem.setText(item.getMenuItem().getEmoji() + " " + item.getMenuItem().getName() +
                    " × " + item.getQuantity());
            tvItem.setTextSize(13f);
            tvItem.setTextColor(0xFF1A1A1A);

            TextView tvPrice = new TextView(row.getContext());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getItemTotal()));
            tvPrice.setTextSize(13f);
            tvPrice.setTextColor(0xFF1A1A1A);
            tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);

            row.addView(tvItem);
            row.addView(tvPrice);
        }
    }
}
