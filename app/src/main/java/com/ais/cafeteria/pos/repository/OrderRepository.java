package com.ais.cafeteria.pos.repository;

import com.ais.cafeteria.pos.models.CartItem;
import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.models.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OrderRepository  —  Data Access Layer for Orders.
 * NoSQL back-end: Firebase Realtime Database
 *
 * Database URL: https://ais-cafeteria-pos-default-rtdb.firebaseio.com
 * Node path: /orders/{orderId}
 *
 * Tier mapping:
 *   UI  →  OrderRepository  →  [Firebase SDK]  →  Realtime Database (NoSQL)
 */
public class OrderRepository {

    // Callback interfaces
    public interface OnOrderSavedCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface OnOrdersLoadedCallback {
        void onSuccess(List<Order> orders);
        void onError(String message);
    }

    private final DatabaseReference ordersRef;

    public OrderRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://ais-cafeteria-pos-default-rtdb.firebaseio.com"
        );
        ordersRef = database.getReference("orders");
    }

    // ── Save Order ────────────────────────────────────────────

    /**
     * Saves a completed order to Firebase Realtime Database.
     * Path: /orders/{orderId}
     */
    public void saveOrder(Order order, OnOrderSavedCallback callback) {
        ordersRef.child(sanitizeKey(order.getOrderId()))
                .setValue(orderToMap(order))
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // ── Fetch All Orders ──────────────────────────────────────

    /**
     * Reads all orders from /orders and returns them as a List.
     * Firebase RTDB returns children in key order — we reverse for newest-first.
     */
    public void getAllOrders(OnOrdersLoadedCallback callback) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Order> orders = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Order order = snapshotToOrder(child);
                    if (order != null) orders.add(0, order); // prepend = newest first
                }
                callback.onSuccess(orders);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ── Search Orders ─────────────────────────────────────────

    /**
     * Fetches all orders then filters in-memory by orderId, date,
     * payment method, or status.
     */
    public void searchOrders(String query, OnOrdersLoadedCallback callback) {
        getAllOrders(new OnOrdersLoadedCallback() {
            @Override
            public void onSuccess(List<Order> allOrders) {
                String lq = query.toLowerCase().trim();
                List<Order> filtered = new ArrayList<>();
                for (Order o : allOrders) {
                    if (o.getOrderId().toLowerCase().contains(lq)
                            || o.getDate().toLowerCase().contains(lq)
                            || o.getPaymentMethod().toLowerCase().contains(lq)
                            || o.getStatus().toLowerCase().contains(lq)) {
                        filtered.add(o);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Firebase keys cannot contain . # $ [ ]
     * Strips the '#' from order IDs like "#1022" → "1022"
     */
    private String sanitizeKey(String orderId) {
        return orderId.replace("#", "").trim();
    }

    /** Converts an Order to a Map for Firebase storage. */
    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId",       order.getOrderId());
        map.put("date",          order.getDate());
        map.put("total",         order.getTotal());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("status",        order.getStatus() != null ? order.getStatus() : "Completed");
        map.put("note",          order.getNote()   != null ? order.getNote()   : "");
        map.put("timestamp",     System.currentTimeMillis());

        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (order.getItems() != null) {
            for (CartItem ci : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id",       ci.getMenuItem().getId());
                itemMap.put("name",     ci.getMenuItem().getName());
                itemMap.put("price",    ci.getMenuItem().getPrice());
                itemMap.put("category", ci.getMenuItem().getCategory());
                itemMap.put("emoji",    ci.getMenuItem().getEmoji());
                itemMap.put("qty",      ci.getQuantity());
                itemsList.add(itemMap);
            }
        }
        map.put("items", itemsList);
        return map;
    }

    /** Converts a Firebase DataSnapshot back into an Order object. */
    @SuppressWarnings("unchecked")
    private Order snapshotToOrder(DataSnapshot snap) {
        try {
            String orderId       = snap.child("orderId").getValue(String.class);
            String date          = snap.child("date").getValue(String.class);
            String paymentMethod = snap.child("paymentMethod").getValue(String.class);
            String status        = snap.child("status").getValue(String.class);
            String note          = snap.child("note").getValue(String.class);
            Double totalDouble   = snap.child("total").getValue(Double.class);
            double total         = totalDouble != null ? totalDouble : 0.0;

            // Deserialise cart items
            List<CartItem> items = new ArrayList<>();
            DataSnapshot itemsSnap = snap.child("items");
            for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                Long   idLong   = itemSnap.child("id").getValue(Long.class);
                String name     = itemSnap.child("name").getValue(String.class);
                Double price    = itemSnap.child("price").getValue(Double.class);
                String category = itemSnap.child("category").getValue(String.class);
                String emoji    = itemSnap.child("emoji").getValue(String.class);
                Long   qtyLong  = itemSnap.child("qty").getValue(Long.class);

                int id  = idLong  != null ? idLong.intValue()  : 0;
                int qty = qtyLong != null ? qtyLong.intValue() : 1;

                MenuItem mi = new MenuItem(
                        id, name != null ? name : "",
                        "", price != null ? price : 0.0,
                        category != null ? category : "",
                        emoji    != null ? emoji    : ""
                );
                items.add(new CartItem(mi, qty));
            }

            Order order = new Order(
                    orderId       != null ? orderId       : snap.getKey(),
                    date          != null ? date          : "",
                    items, total,
                    paymentMethod != null ? paymentMethod : ""
            );
            if (status != null) order.setStatus(status);
            if (note   != null) order.setNote(note);
            return order;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}