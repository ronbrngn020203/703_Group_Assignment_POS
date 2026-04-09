package com.ais.cafeteria.pos.models;

import java.util.List;

public class Order {
    private String orderId;
    private String date;
    private List<CartItem> items;
    private double total;
    private String paymentMethod;
    private String status;
    private String note;

    public Order(String orderId, String date, List<CartItem> items, double total, String paymentMethod) {
        this.orderId = orderId;
        this.date = date;
        this.items = items;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = "Completed";
        this.note = "";
    }

    public String getOrderId()        { return orderId; }
    public String getDate()           { return date; }
    public List<CartItem> getItems()  { return items; }
    public double getTotal()          { return total; }
    public String getPaymentMethod()  { return paymentMethod; }
    public String getStatus()         { return status; }
    public String getNote()           { return note; }
    public void setNote(String note)  { this.note = note; }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) count += item.getQuantity();
        return count;
    }
}