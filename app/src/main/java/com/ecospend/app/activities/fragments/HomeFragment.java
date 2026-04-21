package com.ecospend.app.activities.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecospend.app.R;
import com.ecospend.app.activities.AddEditExpenseActivity;
import com.ecospend.app.activities.ExpenseDetailActivity;
import com.ecospend.app.adapters.TransactionAdapter;
import com.ecospend.app.database.TransactionRepository;
import com.ecospend.app.models.Transaction;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.CurrencyFormatter;
import com.ecospend.app.utils.DateUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class HomeFragment extends Fragment {

    private TransactionRepository repository;
    private TransactionAdapter adapter;

    private TextView tvGreeting, tvUserName, tvTotalBalance, tvTotalExpense;
    private TextView tvBudgetLabel, tvBudgetPercent, tvMonthLabel;
    private LinearProgressIndicator progressBudget;
    private RecyclerView recyclerView;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUserName = view.findViewById(R.id.tv_username);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvBudgetLabel = view.findViewById(R.id.tv_budget_label);
        tvBudgetPercent = view.findViewById(R.id.tv_budget_percent);
        tvMonthLabel = view.findViewById(R.id.tv_month_label);
        progressBudget = view.findViewById(R.id.progress_budget);
        recyclerView = view.findViewById(R.id.rv_recent_transactions);
        emptyState = view.findViewById(R.id.empty_state);

        repository = new TransactionRepository(requireActivity().getApplication());

        setupRecyclerView();
        setGreeting();
        loadUserProfile();
        observeTransactions();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(t -> {
            Intent intent = new Intent(getContext(), ExpenseDetailActivity.class);
            intent.putExtra("transaction_id", t.getId());
            startActivity(intent);
        });
    }

    private void setGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText("Good Morning ☀️");
        else if (hour < 17) tvGreeting.setText("Good Afternoon 🌤");
        else tvGreeting.setText("Good Evening 🌙");

        tvMonthLabel.setText(DateUtils.formatMonthYear(System.currentTimeMillis()));
    }

    private void loadUserProfile() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(Constants.PREFS_NAME, 0);
        String name = prefs.getString(Constants.PREF_USER_NAME, "");
        tvUserName.setText(name.isEmpty() ? "Welcome!" : name);

        double budget = Double.parseDouble(prefs.getString(Constants.PREF_MONTHLY_BUDGET, "0"));
        String currency = prefs.getString(Constants.PREF_BASE_CURRENCY, "MYR");

        long start = DateUtils.getStartOfMonth(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth());
        long end = DateUtils.getEndOfMonth(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth());

        repository.getTotalsInRange(start, end, (expenses, income, cats) -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                double balance = income - expenses;
                tvTotalBalance.setText(CurrencyFormatter.format(balance, currency));
                tvTotalExpense.setText("- " + CurrencyFormatter.format(expenses, currency));

                if (budget > 0) {
                    double pct = Math.min((expenses / budget) * 100, 100);
                    tvBudgetLabel.setText("Budget: " + CurrencyFormatter.format(budget, currency));
                    tvBudgetPercent.setText(String.format("%.0f%% used", pct));
                    progressBudget.setProgress((int) pct);
                    progressBudget.setVisibility(View.VISIBLE);
                    tvBudgetLabel.setVisibility(View.VISIBLE);
                    tvBudgetPercent.setVisibility(View.VISIBLE);
                } else {
                    progressBudget.setVisibility(View.GONE);
                    tvBudgetLabel.setVisibility(View.GONE);
                    tvBudgetPercent.setVisibility(View.GONE);
                }
            });
        });
    }

    private void observeTransactions() {
        repository.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            // Show only most recent 10
            List<Transaction> recent = transactions.size() > 10
                    ? transactions.subList(0, 10)
                    : transactions;
            adapter.submitList(recent);

            if (transactions.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
