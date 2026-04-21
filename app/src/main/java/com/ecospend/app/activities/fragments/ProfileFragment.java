package com.ecospend.app.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ecospend.app.R;
import com.ecospend.app.api.DeepSeekApiClient;
import com.ecospend.app.database.TransactionRepository;
import com.ecospend.app.models.Transaction;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.SpendingAnalyzer;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private EditText etName, etBudget, etDeepseekKey;
    private Spinner spinnerCurrency;
    private Button btnSaveProfile, btnAnalyze;
    private TextView tvAiResponse, tvAvatarInitials;
    private ProgressBar progressAi;
    private MaterialCardView cardAiResult;
    private ScrollView scrollView;

    private SharedPreferences prefs;
    private TransactionRepository repository;
    private List<Transaction> latestTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName           = view.findViewById(R.id.et_profile_name);
        etBudget         = view.findViewById(R.id.et_monthly_budget);
        etDeepseekKey    = view.findViewById(R.id.et_deepseek_key);
        spinnerCurrency  = view.findViewById(R.id.spinner_base_currency);
        btnSaveProfile   = view.findViewById(R.id.btn_save_profile);
        btnAnalyze       = view.findViewById(R.id.btn_analyze_spending);
        tvAiResponse     = view.findViewById(R.id.tv_ai_response);
        tvAvatarInitials = view.findViewById(R.id.tv_avatar_initials);
        progressAi       = view.findViewById(R.id.progress_ai);
        cardAiResult     = view.findViewById(R.id.card_ai_result);
        scrollView       = view.findViewById(R.id.scroll_ai);

        prefs      = requireActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        repository = new TransactionRepository(requireActivity().getApplication());

        setupCurrencySpinner();
        loadSavedProfile();
        observeTransactions();

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnAnalyze.setOnClickListener(v -> runAiAnalysis());
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Constants.CURRENCIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
    }

    private void loadSavedProfile() {
        String name     = prefs.getString(Constants.PREF_USER_NAME, "");
        String budget   = prefs.getString(Constants.PREF_MONTHLY_BUDGET, "");
        String currency = prefs.getString(Constants.PREF_BASE_CURRENCY, "MYR");
        String apiKey   = prefs.getString(Constants.PREF_DEEPSEEK_KEY, "");

        etName.setText(name);
        etBudget.setText(budget);
        etDeepseekKey.setText(apiKey);

        for (int i = 0; i < Constants.CURRENCIES.length; i++) {
            if (Constants.CURRENCIES[i].equals(currency)) { spinnerCurrency.setSelection(i); break; }
        }

        String initials = name.isEmpty() ? "ME"
                : name.substring(0, Math.min(2, name.length())).toUpperCase();
        tvAvatarInitials.setText(initials);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedProfile();
    }

    private void observeTransactions() {
        repository.getAllTransactions().observe(getViewLifecycleOwner(),
                transactions -> latestTransactions = transactions != null ? transactions : new ArrayList<>());
    }

    private void saveProfile() {
        String name     = etName.getText().toString().trim();
        String budget   = etBudget.getText().toString().trim();
        String currency = Constants.CURRENCIES[spinnerCurrency.getSelectedItemPosition()];
        String apiKey   = etDeepseekKey.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Please enter your name"); return; }

        prefs.edit()
             .putString(Constants.PREF_USER_NAME, name)
             .putString(Constants.PREF_MONTHLY_BUDGET, budget.isEmpty() ? "0" : budget)
             .putString(Constants.PREF_BASE_CURRENCY, currency)
             .putString(Constants.PREF_DEEPSEEK_KEY, apiKey)
             .apply();

        tvAvatarInitials.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
        Toast.makeText(getContext(), "Profile saved ✓", Toast.LENGTH_SHORT).show();
    }

    private void runAiAnalysis() {
        String apiKey   = prefs.getString(Constants.PREF_DEEPSEEK_KEY, "");
        String name     = prefs.getString(Constants.PREF_USER_NAME, "User");
        String currency = prefs.getString(Constants.PREF_BASE_CURRENCY, "MYR");
        double budget   = 0;
        try { budget = Double.parseDouble(prefs.getString(Constants.PREF_MONTHLY_BUDGET, "0")); }
        catch (Exception ignored) {}

        progressAi.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(false);
        cardAiResult.setVisibility(View.GONE);

        String context = SpendingAnalyzer.buildAnalysisContext(latestTransactions, budget, currency, name);

        new DeepSeekApiClient(apiKey).analyzeSpending(context, new DeepSeekApiClient.AiCallback() {
            @Override public void onSuccess(String response) {
                if (!isAdded()) return;
                progressAi.setVisibility(View.GONE);
                btnAnalyze.setEnabled(true);
                tvAiResponse.setText(response);
                cardAiResult.setVisibility(View.VISIBLE);
                scrollView.post(() -> scrollView.smoothScrollTo(0, cardAiResult.getTop()));
            }
            @Override public void onError(String error) {
                if (!isAdded()) return;
                progressAi.setVisibility(View.GONE);
                btnAnalyze.setEnabled(true);
                tvAiResponse.setText(error);
                cardAiResult.setVisibility(View.VISIBLE);
            }
        });
    }
}
