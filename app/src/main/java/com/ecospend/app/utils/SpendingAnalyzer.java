package com.ecospend.app.utils;

import com.ecospend.app.models.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpendingAnalyzer {

    public static String buildAnalysisContext(List<Transaction> transactions,
                                               double monthlyBudget,
                                               String baseCurrency,
                                               String userName) {
        if (transactions == null || transactions.isEmpty()) {
            return "The user has no recorded transactions yet. Greet them warmly and encourage them to start tracking their expenses to get personalized insights.";
        }

        double totalExpense = 0;
        double totalIncome = 0;
        Map<String, Double> categoryTotals = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (Transaction t : transactions) {
            if (Constants.TYPE_EXPENSE.equals(t.getType())) {
                totalExpense += t.getAmountInMYR();
                categoryTotals.merge(t.getCategory(), t.getAmountInMYR(), Double::sum);
                categoryCounts.merge(t.getCategory(), 1, Integer::sum);
            } else {
                totalIncome += t.getAmountInMYR();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("User profile: ").append(userName != null ? userName : "User").append("\n");
        sb.append("Base currency: ").append(baseCurrency).append("\n");
        sb.append("Monthly budget: ").append(CurrencyFormatter.format(monthlyBudget, "MYR")).append("\n\n");

        sb.append("This month's summary (amounts in MYR):\n");
        sb.append("- Total expenses: ").append(CurrencyFormatter.format(totalExpense, "MYR")).append("\n");
        sb.append("- Total income: ").append(CurrencyFormatter.format(totalIncome, "MYR")).append("\n");
        sb.append("- Net balance: ").append(CurrencyFormatter.format(totalIncome - totalExpense, "MYR")).append("\n");

        if (monthlyBudget > 0) {
            double budgetUsed = (totalExpense / monthlyBudget) * 100;
            sb.append("- Budget used: ").append(String.format("%.1f%%", budgetUsed)).append("\n");
        }

        sb.append("\nSpending breakdown by category:\n");

        final double finalTotalExpense = totalExpense;
        // Sort by amount descending
        categoryTotals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    double pct = finalTotalExpense > 0 ? (entry.getValue() / finalTotalExpense) * 100 : 0;
                    sb.append("- ").append(entry.getKey())
                      .append(": ").append(CurrencyFormatter.format(entry.getValue(), "MYR"))
                      .append(" (").append(String.format("%.1f%%", pct)).append(", ")
                      .append(categoryCounts.getOrDefault(entry.getKey(), 0)).append(" transactions)\n");
                });

        sb.append("\nNumber of transactions this month: ").append(transactions.size()).append("\n");
        sb.append("\nPlease provide personalized financial advice for this user.");

        return sb.toString();
    }
}
