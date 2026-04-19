package com.ais.cafeteria.pos.models;

import java.util.List;

public class Order {
    private String orderId;
    private String date;
    private List<CartItem> items;
    private double total;
    private String paymentMethod;
    private String staffId;
    private String status;
    private String note;

    /** Required by Firebase — do not remove */
    public Order() {}

    public Order(String orderId, String date, List<CartItem> items,
                 double total, String paymentMethod) {
        this.orderId       = orderId;
        this.date          = date;
        this.items         = items;
        this.total         = total;
        this.paymentMethod = paymentMethod;
        this.status        = paymentMethod.equals("Cash") ? "Pending" : "Completed";
        this.note          = "";
    }

    public String         getOrderId()       { return orderId; }
    public String         getDate()          { return date; }
    public List<CartItem> getItems()         { return items; }
    public double         getTotal()         { return total; }
    public String         getPaymentMethod() { return paymentMethod; }
    public String         getStaffId()       { return staffId; }
    public String         getStatus()        { return status; }
    public String         getNote()          { return note; }

    public void setStaffId(String staffId) { this.staffId = staffId; }
    public void setNote(String note)     { this.note   = note; }
    public void setStatus(String status) { this.status = status; }
    public void setTotal(double total)   { this.total  = total; }

    public int getItemCount() {
        int count = 0;
        if (items != null) for (CartItem item : items) count += item.getQuantity();
        return count;
    }
}
