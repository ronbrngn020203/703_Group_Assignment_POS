package com.ais.cafeteria.pos.models;

/**
 * MenuItem  —  Represents a single item on the AIS Cafeteria menu.
 *
 * Changes from original:
 *  • Added no-arg constructor (required by Gson for Retrofit deserialization)
 *  • Added setters (required for editing via EditMenuItemActivity + Firestore)
 */
public class MenuItem {
    private int    id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String emoji;

    /** Required by Gson — do not remove */
    public MenuItem() {}

    public MenuItem(int id, String name, String description, double price,
                    String category, String emoji) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.price       = price;
        this.category    = category;
        this.emoji       = emoji;
    }

    // ── Getters ───────────────────────────────────────────────
    public int    getId()          { return id; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public double getPrice()       { return price; }
    public String getCategory()    { return category; }
    public String getEmoji()       { return emoji; }

    // ── Setters (for EditMenuItemActivity) ────────────────────
    public void setId(int id)                   { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setDescription(String desc)     { this.description = desc; }
    public void setPrice(double price)          { this.price = price; }
    public void setCategory(String category)    { this.category = category; }
    public void setEmoji(String emoji)          { this.emoji = emoji; }
}