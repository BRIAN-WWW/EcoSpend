package com.ecospend.app.activities.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecospend.app.R;
import com.ecospend.app.api.CurrencyApiClient;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.CurrencyFormatter;
import com.ecospend.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrencyFragment extends Fragment {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private TextView tvResult, tvRate, tvLastUpdated;
    private ImageButton btnSwap;
    private ProgressBar progressBar;
    private RecyclerView rvRates;

    private CurrencyApiClient apiClient;
    private Map<String, Double> cachedRates;
    private String fromCurrency = "MYR";
    private String toCurrency = "USD";
    private boolean isUpdating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_currency, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAmount = view.findViewById(R.id.et_amount_convert);
        spinnerFrom = view.findViewById(R.id.spinner_from_currency);
        spinnerTo = view.findViewById(R.id.spinner_to_currency);
        tvResult = view.findViewById(R.id.tv_conversion_result);
        tvRate = view.findViewById(R.id.tv_exchange_rate);
        tvLastUpdated = view.findViewById(R.id.tv_last_updated);
        btnSwap = view.findViewById(R.id.btn_swap_currency);
        progressBar = view.findViewById(R.id.progress_currency);
        rvRates = view.findViewById(R.id.rv_rates);

        // API Client now requires Context
        apiClient = new CurrencyApiClient(requireContext());

        setupSpinners();
        setupSwapButton();
        setupAmountInput();
        fetchRates();
    }

    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Constants.CURRENCIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(1);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdating) {
                    fromCurrency = Constants.CURRENCIES[spinnerFrom.getSelectedItemPosition()];
                    toCurrency = Constants.CURRENCIES[spinnerTo.getSelectedItemPosition()];
                    fetchRates();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerFrom.setOnItemSelectedListener(listener);
        spinnerTo.setOnItemSelectedListener(listener);
    }

    private void setupSwapButton() {
        btnSwap.setOnClickListener(v -> {
            isUpdating = true;
            int fromPos = spinnerFrom.getSelectedItemPosition();
            int toPos = spinnerTo.getSelectedItemPosition();
            spinnerFrom.setSelection(toPos);
            spinnerTo.setSelection(fromPos);
            fromCurrency = Constants.CURRENCIES[toPos];
            toCurrency = Constants.CURRENCIES[fromPos];
            isUpdating = false;
            fetchRates();
        });
    }

    private void setupAmountInput() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (cachedRates != null) {
                    performConversion();
                }
            }
        });
    }

    private void fetchRates() {
        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("...");

        apiClient.getRates(fromCurrency, new CurrencyApiClient.RatesCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates, String base, boolean isOffline, long lastSyncTimestamp) {
                if (!isAdded()) return;
                cachedRates = rates;
                progressBar.setVisibility(View.GONE);

                if (isOffline) {
                    tvLastUpdated.setText("Offline: Using cross-rates from " + DateUtils.formatDate(lastSyncTimestamp));
                    tvLastUpdated.setTextColor(Color.RED);
                } else {
                    tvLastUpdated.setText("Rates updated just now");
                    tvLastUpdated.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
                }

                performConversion();
                updateRatesList(rates);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                tvResult.setText("Error");
                tvLastUpdated.setText("No internet and no offline data available.");
                tvLastUpdated.setTextColor(Color.RED);
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performConversion() {
        if (cachedRates == null) return;
        String input = etAmount.getText().toString();
        if (input.isEmpty()) {
            tvResult.setText("0.00");
            tvRate.setText("");
            return;
        }
        try {
            double amount = Double.parseDouble(input);
            if (cachedRates.containsKey(toCurrency)) {
                double rate = cachedRates.get(toCurrency);
                double result = amount * rate;
                tvResult.setText(CurrencyFormatter.format(result, toCurrency));
                tvRate.setText("1 " + fromCurrency + " = "
                        + String.format("%.4f", rate) + " " + toCurrency);
            }
        } catch (NumberFormatException e) {
            tvResult.setText("Invalid input");
        }
    }

    private void updateRatesList(Map<String, Double> rates) {
        List<RateItem> items = new ArrayList<>();
        String[] popular = {"USD", "EUR", "GBP", "SGD", "JPY", "CNY", "AUD"};
        for (String cur : popular) {
            if (!cur.equals(fromCurrency) && rates.containsKey(cur)) {
                items.add(new RateItem(cur, rates.get(cur)));
            }
        }

        RatesListAdapter ratesAdapter = new RatesListAdapter(items, fromCurrency);
        rvRates.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRates.setAdapter(ratesAdapter);
    }

    public static class RateItem {
        public String currency;
        public double rate;
        RateItem(String c, double r) { currency = c; rate = r; }
    }

    public static class RatesListAdapter extends RecyclerView.Adapter<RatesListAdapter.VH> {
        private final List<RateItem> items;
        private final String base;

        RatesListAdapter(List<RateItem> items, String base) {
            this.items = items;
            this.base = base;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rate, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            RateItem item = items.get(position);
            holder.tvCurrency.setText(item.currency);
            holder.tvRate.setText("1 " + base + " = " + String.format("%.4f", item.rate));
            holder.tvSymbol.setText(CurrencyFormatter.getSymbol(item.currency));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCurrency, tvRate, tvSymbol;
            VH(@NonNull View v) {
                super(v);
                tvCurrency = v.findViewById(R.id.tv_rate_currency);
                tvRate = v.findViewById(R.id.tv_rate_value);
                tvSymbol = v.findViewById(R.id.tv_rate_symbol);
            }
        }
    }
}