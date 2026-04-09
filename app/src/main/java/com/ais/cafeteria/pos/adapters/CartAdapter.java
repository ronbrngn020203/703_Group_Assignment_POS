package com.ais.cafeteria.pos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.models.CartItem;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartListener {
        void onIncrement(CartItem item);
        void onDecrement(CartItem item);
        void onRemove(CartItem item);
    }

    private List<CartItem> cartItems;
    private final CartListener listener;

    public CartAdapter(List<CartItem> cartItems, CartListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvItemTotal, tvQty;
        TextView btnMinus, btnPlus, btnRemove;

        CartViewHolder(View itemView) {
            super(itemView);
            tvEmoji     = itemView.findViewById(R.id.tvEmoji);
            tvName      = itemView.findViewById(R.id.tvName);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
            tvQty       = itemView.findViewById(R.id.tvQty);
            btnMinus    = itemView.findViewById(R.id.btnMinus);
            btnPlus     = itemView.findViewById(R.id.btnPlus);
            btnRemove   = itemView.findViewById(R.id.btnRemove);
        }

        void bind(CartItem item) {
            tvEmoji.setText(item.getMenuItem().getEmoji());
            tvName.setText(item.getMenuItem().getName());
            tvQty.setText(String.valueOf(item.getQuantity()));
            tvItemTotal.setText(String.format(Locale.getDefault(),
                    "$%.2f", item.getItemTotal()));

            btnPlus.setOnClickListener(v -> listener.onIncrement(item));
            btnMinus.setOnClickListener(v -> listener.onDecrement(item));
            btnRemove.setOnClickListener(v -> listener.onRemove(item));
        }
    }
}