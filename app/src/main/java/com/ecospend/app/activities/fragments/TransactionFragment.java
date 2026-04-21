package com.ecospend.app.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecospend.app.R;
import com.ecospend.app.activities.ExpenseDetailActivity;
import com.ecospend.app.adapters.TransactionAdapter;
import com.ecospend.app.database.TransactionRepository;
import com.ecospend.app.models.Transaction;
import com.ecospend.app.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionFragment extends Fragment {

    private TransactionRepository repository;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();

    private EditText etSearch;
    private ChipGroup chipGroup;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvCount;

    private String selectedType = "all";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.et_search);
        chipGroup = view.findViewById(R.id.chip_group_filter);
        recyclerView = view.findViewById(R.id.rv_transactions);
        emptyState = view.findViewById(R.id.empty_state_transactions);
        tvCount = view.findViewById(R.id.tv_transaction_count);

        repository = new TransactionRepository(requireActivity().getApplication());

        setupRecyclerView();
        setupSearch();
        setupChips(view);
        observeTransactions();
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

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().toLowerCase();
                applyFilters();
            }
        });
    }

    private void setupChips(View view) {
        Chip chipAll = view.findViewById(R.id.chip_all);
        Chip chipExpense = view.findViewById(R.id.chip_expense);
        Chip chipIncome = view.findViewById(R.id.chip_income);

        chipAll.setOnClickListener(v -> { selectedType = "all"; applyFilters(); });
        chipExpense.setOnClickListener(v -> { selectedType = Constants.TYPE_EXPENSE; applyFilters(); });
        chipIncome.setOnClickListener(v -> { selectedType = Constants.TYPE_INCOME; applyFilters(); });
    }

    private void observeTransactions() {
        repository.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            allTransactions = transactions != null ? transactions : new ArrayList<>();
            applyFilters();
        });
    }

    private void applyFilters() {
        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> {
                    boolean typeMatch = selectedType.equals("all") || t.getType().equals(selectedType);
                    boolean searchMatch = searchQuery.isEmpty()
                            || t.getTitle().toLowerCase().contains(searchQuery)
                            || t.getCategory().toLowerCase().contains(searchQuery)
                            || (t.getNotes() != null && t.getNotes().toLowerCase().contains(searchQuery));
                    return typeMatch && searchMatch;
                })
                .collect(Collectors.toList());

        adapter.submitList(filtered);
        tvCount.setText(filtered.size() + " transactions");

        if (filtered.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
