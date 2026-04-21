package com.ecospend.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {

    @PrimaryKey
    private int id = 1; // single profile

    private String name;
    private String baseCurrency;
    private double monthlyBudget;
    private String avatarInitials;

    public UserProfile(String name, String baseCurrency, double monthlyBudget) {
        this.name = name;
        this.baseCurrency = baseCurrency;
        this.monthlyBudget = monthlyBudget;
        this.avatarInitials = name != null && !name.isEmpty()
                ? name.substring(0, Math.min(2, name.length())).toUpperCase()
                : "ME";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.avatarInitials = name != null && !name.isEmpty()
                ? name.substring(0, Math.min(2, name.length())).toUpperCase()
                : "ME";
    }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }
}
