package com.ais.cafeteria.pos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ais.cafeteria.pos.R;
import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.utils.CartManager;

import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    // ── Interfaces ────────────────────────────────────────────
    public interface OnAddToCartListener {
        void onAddToCart(MenuItem item);
    }

    /** NEW: Long-press a menu card to open the edit screen */
    public interface OnItemLongClickListener {
        void onLongClick(MenuItem item);
    }

    // ── Fields ────────────────────────────────────────────────
    private List<MenuItem> menuItems;
    private final OnAddToCartListener listener;
    private OnItemLongClickListener longClickListener; // NEW

    // ── Constructor ───────────────────────────────────────────
    public MenuAdapter(List<MenuItem> menuItems, OnAddToCartListener listener) {
        this.menuItems = menuItems;
        this.listener  = listener;
    }

    /** NEW: Set this from MenuActivity to enable long-press editing */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateItems(List<MenuItem> newItems) {
        this.menuItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(menuItems.get(position));
    }

    @Override
    public int getItemCount() {
        return menuItems != null ? menuItems.size() : 0;
    }

    // ── ViewHolder ────────────────────────────────────────────
    class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDesc, tvPrice, tvInCart;
        Button   btnAdd;

        MenuViewHolder(View itemView) {
            super(itemView);
            tvEmoji  = itemView.findViewById(R.id.tvEmoji);
            tvName   = itemView.findViewById(R.id.tvName);
            tvDesc   = itemView.findViewById(R.id.tvDesc);
            tvPrice  = itemView.findViewById(R.id.tvPrice);
            tvInCart = itemView.findViewById(R.id.tvInCart);
            btnAdd   = itemView.findViewById(R.id.btnAdd);
        }

        void bind(MenuItem item) {
            tvEmoji.setText(item.getEmoji());
            tvName.setText(item.getName());
            tvDesc.setText(item.getDescription());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrice()));

            boolean inCart = CartManager.getInstance().isInCart(item.getId());
            if (inCart) {
                int qty = CartManager.getInstance().getItemQtyInCart(item.getId());
                tvInCart.setVisibility(View.VISIBLE);
                tvInCart.setText("✓ In cart (" + qty + ")");
            } else {
                tvInCart.setVisibility(View.GONE);
            }

            // onClick — Add to cart
            btnAdd.setOnClickListener(v -> {
                listener.onAddToCart(item);
                notifyItemChanged(getAdapterPosition());
            });

            // onLongClick — Open edit screen (NEW) or show toast as fallback
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(item);
                } else {
                    android.widget.Toast.makeText(v.getContext(),
                            item.getName() + " — " + item.getDescription(),
                            android.widget.Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
    }
}