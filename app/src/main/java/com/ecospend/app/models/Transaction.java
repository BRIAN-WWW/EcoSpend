package com.ecospend.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private double amount;
    private String category;
    private String type; // "expense" or "income"
    private String currency;
    private String notes;
    private long date; // stored as timestamp
    private double amountInMYR; // converted base amount

    // Constructor
    public Transaction(String title, double amount, String category, String type,
                       String currency, String notes, long date, double amountInMYR) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.currency = currency;
        this.notes = notes;
        this.date = date;
        this.amountInMYR = amountInMYR;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public double getAmountInMYR() { return amountInMYR; }
    public void setAmountInMYR(double amountInMYR) { this.amountInMYR = amountInMYR; }
}
