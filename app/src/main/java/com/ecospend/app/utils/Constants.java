package com.ecospend.app.utils;

import com.ecospend.app.R;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constants {

    public static final String PREFS_NAME = "EcoSpendPrefs";
    public static final String PREF_DEEPSEEK_KEY = "deepseek_api_key";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_BASE_CURRENCY = "base_currency";
    public static final String PREF_MONTHLY_BUDGET = "monthly_budget";
    public static final String PREF_ONBOARDING_DONE = "onboarding_done";

    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";

    // Category names
    public static final String CAT_FOOD = "Food & Dining";
    public static final String CAT_TRANSPORT = "Transport";
    public static final String CAT_SHOPPING = "Shopping";
    public static final String CAT_BILLS = "Bills & Utilities";
    public static final String CAT_ENTERTAINMENT = "Entertainment";
    public static final String CAT_HEALTH = "Health";
    public static final String CAT_EDUCATION = "Education";
    public static final String CAT_TRAVEL = "Travel";
    public static final String CAT_SUBSCRIPTION = "Subscriptions";
    public static final String CAT_SALARY = "Salary";
    public static final String CAT_FREELANCE = "Freelance";
    public static final String CAT_OTHER = "Other";

    public static final String[] EXPENSE_CATEGORIES = {
        CAT_FOOD, CAT_TRANSPORT, CAT_SHOPPING, CAT_BILLS,
        CAT_ENTERTAINMENT, CAT_HEALTH, CAT_EDUCATION,
        CAT_TRAVEL, CAT_SUBSCRIPTION, CAT_OTHER
    };

    public static final String[] INCOME_CATEGORIES = {
        CAT_SALARY, CAT_FREELANCE, CAT_OTHER
    };

    // Popular currencies
    public static final String[] CURRENCIES = {
        "MYR", "USD", "EUR", "GBP", "SGD", "JPY", "CNY",
        "AUD", "CAD", "HKD", "THB", "IDR", "KRW", "INR"
    };

    public static int getCategoryIcon(String category) {
        switch (category) {
            case CAT_FOOD: return R.drawable.ic_food;
            case CAT_TRANSPORT: return R.drawable.ic_transport;
            case CAT_SHOPPING: return R.drawable.ic_shopping;
            case CAT_BILLS: return R.drawable.ic_bills;
            case CAT_ENTERTAINMENT: return R.drawable.ic_entertainment;
            case CAT_HEALTH: return R.drawable.ic_health;
            case CAT_EDUCATION: return R.drawable.ic_education;
            case CAT_TRAVEL: return R.drawable.ic_travel;
            case CAT_SUBSCRIPTION: return R.drawable.ic_subscription;
            case CAT_SALARY: return R.drawable.ic_salary;
            case CAT_FREELANCE: return R.drawable.ic_freelance;
            default: return R.drawable.ic_other;
        }
    }

    public static int getCategoryColor(String category) {
        switch (category) {
            case CAT_FOOD: return 0xFFFF6B6B;
            case CAT_TRANSPORT: return 0xFF4ECDC4;
            case CAT_SHOPPING: return 0xFF45B7D1;
            case CAT_BILLS: return 0xFFF7DC6F;
            case CAT_ENTERTAINMENT: return 0xFFBB8FCE;
            case CAT_HEALTH: return 0xFF82E0AA;
            case CAT_EDUCATION: return 0xFFF0B27A;
            case CAT_TRAVEL: return 0xFF85C1E9;
            case CAT_SUBSCRIPTION: return 0xFFAEB6BF;
            case CAT_SALARY: return 0xFF58D68D;
            case CAT_FREELANCE: return 0xFF48C9B0;
            default: return 0xFFAEB6BF;
        }
    }

    public static final int[] CHART_COLORS = {
        0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1, 0xFFF7DC6F,
        0xFFBB8FCE, 0xFF82E0AA, 0xFFF0B27A, 0xFF85C1E9,
        0xFFAEB6BF, 0xFF58D68D
    };
}
