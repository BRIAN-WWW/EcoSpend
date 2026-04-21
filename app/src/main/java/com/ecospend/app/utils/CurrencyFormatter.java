package com.ecospend.app.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencyFormatter {

    private static final DecimalFormat DF = new DecimalFormat("#,##0.00");
    private static final Map<String, String> SYMBOLS = new HashMap<>();

    static {
        SYMBOLS.put("MYR", "RM");
        SYMBOLS.put("USD", "$");
        SYMBOLS.put("EUR", "€");
        SYMBOLS.put("GBP", "£");
        SYMBOLS.put("SGD", "S$");
        SYMBOLS.put("JPY", "¥");
        SYMBOLS.put("CNY", "¥");
        SYMBOLS.put("AUD", "A$");
        SYMBOLS.put("CAD", "C$");
        SYMBOLS.put("HKD", "HK$");
        SYMBOLS.put("THB", "฿");
        SYMBOLS.put("IDR", "Rp");
        SYMBOLS.put("KRW", "₩");
        SYMBOLS.put("INR", "₹");
    }

    public static String format(double amount, String currency) {
        String symbol = SYMBOLS.getOrDefault(currency, currency + " ");
        if (currency.equals("JPY") || currency.equals("KRW") || currency.equals("IDR")) {
            return symbol + String.format(Locale.getDefault(), "%,.0f", amount);
        }
        return symbol + DF.format(amount);
    }

    public static String getSymbol(String currency) {
        return SYMBOLS.getOrDefault(currency, currency);
    }

    public static String formatCompact(double amount) {
        if (Math.abs(amount) >= 1_000_000) {
            return String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000);
        } else if (Math.abs(amount) >= 1_000) {
            return String.format(Locale.getDefault(), "%.1fK", amount / 1_000);
        }
        return DF.format(amount);
    }
}
