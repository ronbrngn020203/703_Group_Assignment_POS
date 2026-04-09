package com.ais.cafeteria.pos.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.models.Order;

import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orders;

    public OrderHistoryAdapter(List<Order> orders) {
        this.orders = orders;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() { return orders != null ? orders.size() : 0; }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderMeta, tvStatus, tvTotal;
        TextView btnViewDetails;

        OrderViewHolder(View v) {
            super(v);
            tvOrderId      = v.findViewById(R.id.tvOrderId);
            tvOrderMeta    = v.findViewById(R.id.tvOrderMeta);
            tvStatus       = v.findViewById(R.id.tvStatus);
            tvTotal        = v.findViewById(R.id.tvTotal);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
        }

        void bind(Order order) {
            tvOrderId.setText(order.getOrderId());
            tvOrderMeta.setText(order.getDate() + " · " + order.getItemCount() + " items");
            tvStatus.setText(order.getStatus());
            double total = order.getTotal() * 1.15;
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));

            btnViewDetails.setOnClickListener(v -> {
                double subtotal = order.getTotal();
                double gst      = subtotal * 0.15;

                // Build message
                StringBuilder message = new StringBuilder();
                message.append("📋 Order: ").append(order.getOrderId()).append("\n");
                message.append("📅 Date: ").append(order.getDate()).append("\n");
                message.append("💳 Payment: ").append(order.getPaymentMethod()).append("\n");
                message.append("━━━━━━━━━━━━━━━━━━━━\n");
                message.append("Subtotal:   $").append(String.format(Locale.getDefault(), "%.2f", subtotal)).append("\n");
                message.append("GST (15%): $").append(String.format(Locale.getDefault(), "%.2f", gst)).append("\n");
                message.append("━━━━━━━━━━━━━━━━━━━━\n");
                message.append("Total Paid: $").append(String.format(Locale.getDefault(), "%.2f", total)).append("\n");
                message.append("Status: ").append(order.getStatus());

                // Show dialog
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Order " + order.getOrderId())
                        .setMessage(message.toString())
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
    }
}