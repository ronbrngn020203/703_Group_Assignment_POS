package com.ais.cafeteria.pos.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ais.cafeteria.pos.models.CartItem;
import com.ais.cafeteria.pos.models.MenuItem;
import com.ais.cafeteria.pos.models.Order;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartManager {

    private static CartManager instance;
    private List<CartItem> cartItems = new ArrayList<>();
    private List<Order> orderHistory = new ArrayList<>();
    private int orderCounter = 1021;
    private Order lastOrder;
    private SharedPreferences prefs;
    private String pendingNote = ""; // ✅ stores note from cart

    private static final String PREF_NAME   = "AIS_CART_PREFS";
    private static final String KEY_HISTORY = "order_history";
    private static final String KEY_COUNTER = "order_counter";

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void init(Context context) {
        if (prefs != null) return;
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        orderCounter = prefs.getInt(KEY_COUNTER, 1021);
        loadOrderHistory();
    }

    // ✅ Set note from CartActivity
    public void setOrderNote(String note) {
        this.pendingNote = note != null ? note : "";
    }

    public String getOrderNote() {
        return pendingNote;
    }

    // ── Save & Load ───────────────────────────────────────────

    private void saveOrderHistory() {
        try {
            JSONArray array = new JSONArray();
            for (Order order : orderHistory) {
                JSONObject obj = new JSONObject();
                obj.put("orderId",       order.getOrderId());
                obj.put("date",          order.getDate());
                obj.put("total",         order.getTotal());
                obj.put("paymentMethod", order.getPaymentMethod());
                obj.put("status",        order.getStatus());
                obj.put("note",          order.getNote() != null ? order.getNote() : "");

                JSONArray itemsArray = new JSONArray();
                for (CartItem item : order.getItems()) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("id",       item.getMenuItem().getId());
                    itemObj.put("name",     item.getMenuItem().getName());
                    itemObj.put("price",    item.getMenuItem().getPrice());
                    itemObj.put("category", item.getMenuItem().getCategory());
                    itemObj.put("emoji",    item.getMenuItem().getEmoji());
                    itemObj.put("desc",     item.getMenuItem().getDescription());
                    itemObj.put("qty",      item.getQuantity());
                    itemsArray.put(itemObj);
                }
                obj.put("items", itemsArray);
                array.put(obj);
            }
            prefs.edit().putString(KEY_HISTORY, array.toString()).apply();
            prefs.edit().putInt(KEY_COUNTER, orderCounter).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrderHistory() {
        try {
            String json = prefs.getString(KEY_HISTORY, null);
            if (json == null) {
                orderHistory.add(new Order("#1020", "10 Mar 2026", new ArrayList<>(), 14.00, "Card"));
                orderHistory.add(new Order("#1021", "11 Mar 2026", new ArrayList<>(), 22.50, "Cash"));
                return;
            }
            JSONArray array = new JSONArray(json);
            orderHistory.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                JSONArray itemsArray = obj.getJSONArray("items");
                List<CartItem> items = new ArrayList<>();
                for (int j = 0; j < itemsArray.length(); j++) {
                    JSONObject itemObj = itemsArray.getJSONObject(j);
                    MenuItem mi = new MenuItem(
                            itemObj.getInt("id"),
                            itemObj.getString("name"),
                            itemObj.getString("desc"),
                            itemObj.getDouble("price"),
                            itemObj.getString("category"),
                            itemObj.getString("emoji")
                    );
                    items.add(new CartItem(mi, itemObj.getInt("qty")));
                }

                Order order = new Order(
                        obj.getString("orderId"),
                        obj.getString("date"),
                        items,
                        obj.getDouble("total"),
                        obj.getString("paymentMethod")
                );
                order.setNote(obj.optString("note", ""));
                orderHistory.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Cart Operations ───────────────────────────────────────

    public void addItem(MenuItem menuItem) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId() == menuItem.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(menuItem, 1));
    }

    public void removeItem(int menuItemId) {
        cartItems.removeIf(item -> item.getMenuItem().getId() == menuItemId);
    }

    public void updateQuantity(int menuItemId, int delta) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId() == menuItemId) {
                int newQty = item.getQuantity() + delta;
                if (newQty <= 0) {
                    removeItem(menuItemId);
                } else {
                    item.setQuantity(newQty);
                }
                return;
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
        pendingNote = ""; // ✅ clear note after order
    }

    public boolean isInCart(int menuItemId) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId() == menuItemId) return true;
        }
        return false;
    }

    public int getItemQtyInCart(int menuItemId) {
        for (CartItem item : cartItems) {
            if (item.getMenuItem().getId() == menuItemId) return item.getQuantity();
        }
        return 0;
    }

    public List<CartItem> getCartItems() { return cartItems; }

    public int getCartCount() {
        int count = 0;
        for (CartItem item : cartItems) count += item.getQuantity();
        return count;
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) total += item.getItemTotal();
        return total;
    }

    public double getGst()   { return getSubtotal() * 0.15; }
    public double getTotal() { return getSubtotal() + getGst(); }

    // ── Order Operations ──────────────────────────────────────

    public Order placeOrder(String paymentMethod) {
        return placeOrder(paymentMethod, pendingNote); // ✅ use pending note
    }

    public Order placeOrder(String paymentMethod, String note) {
        orderCounter++;
        String orderId = "#" + orderCounter;
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        Order order = new Order(orderId, date, new ArrayList<>(cartItems), getSubtotal(), paymentMethod);
        order.setNote(note);
        orderHistory.add(0, order);
        lastOrder = order;
        clearCart();
        saveOrderHistory();
        return order;
    }

    public Order getLastOrder()          { return lastOrder; }
    public List<Order> getOrderHistory() { return orderHistory; }

    // ── Menu Data ─────────────────────────────────────────────

    public static List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(1,  "Butter Chicken Rice",  "Creamy butter chicken with steamed rice",          12.00, "Mains",    "🍛"));
        items.add(new MenuItem(2,  "Chicken Rice Bowl",    "Grilled chicken, steamed rice, seasonal veggies",   8.50, "Mains",    "🍱"));
        items.add(new MenuItem(3,  "Beef Burger",          "100% beef patty, lettuce, tomato, cheese",          9.00, "Mains",    "🍔"));
        items.add(new MenuItem(4,  "Veggie Wrap",          "Fresh vegetables, hummus, whole wheat wrap",         7.50, "Mains",    "🌯"));
        items.add(new MenuItem(5,  "Fish & Chips",         "Battered fish fillet, golden chips, tartar sauce",  10.00, "Mains",   "🐟"));
        items.add(new MenuItem(6,  "Caesar Salad",         "Romaine, croutons, parmesan, Caesar dressing",       7.00, "Salads",  "🥗"));
        items.add(new MenuItem(7,  "Garden Salad",         "Mixed greens, cherry tomatoes, cucumber",            6.00, "Salads",  "🥬"));
        items.add(new MenuItem(8,  "Cheese Pizza Slice",   "Mozzarella, tomato sauce, fresh basil",              5.00, "Snacks",  "🍕"));
        items.add(new MenuItem(9,  "Spring Rolls (3pc)",   "Crispy fried rolls with sweet chilli sauce",         5.50, "Snacks",  "🥟"));
        items.add(new MenuItem(10, "Latte",                "Double shot espresso with steamed milk",             4.50, "Drinks",  "☕"));
        items.add(new MenuItem(11, "Smoothie",             "Mixed berry, banana, yoghurt blend",                 5.50, "Drinks",  "🥤"));
        items.add(new MenuItem(12, "Water Bottle",         "500ml chilled mineral water",                        2.00, "Drinks",  "💧"));
        items.add(new MenuItem(13, "Chocolate Muffin",     "Rich double chocolate muffin",                       3.50, "Desserts","🧁"));
        items.add(new MenuItem(14, "Fruit Cup",            "Seasonal fresh fruit medley",                        4.00, "Desserts","🍓"));
        return items;
    }
}