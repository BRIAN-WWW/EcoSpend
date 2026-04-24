package com.ecospend.app.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecospend.app.R;
import com.ecospend.app.api.CurrencyApiClient;
import com.ecospend.app.database.TransactionRepository;
import com.ecospend.app.models.Transaction;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.DateUtils;

import java.util.Calendar;

public class AddEditExpenseActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etNotes;
    private Spinner spinnerCategory, spinnerCurrency;
    private RadioGroup rgType;
    private TextView tvDate;
    private Button btnSave;
    private Toolbar toolbar;

    private TransactionRepository repository;
    private CurrencyApiClient currencyClient;

    private long selectedDate = System.currentTimeMillis();
    private boolean isEditMode = false;
    private int editTransactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTitle = findViewById(R.id.et_title);
        etAmount = findViewById(R.id.et_amount);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerCurrency = findViewById(R.id.spinner_currency);
        rgType = findViewById(R.id.rg_type);
        tvDate = findViewById(R.id.tv_selected_date);
        btnSave = findViewById(R.id.btn_save_transaction);

        repository = new TransactionRepository(getApplication());
        // Pass Context to API Client
        currencyClient = new CurrencyApiClient(this);

        setupSpinners();
        setupDatePicker();
        setupTypeToggle();

        editTransactionId = getIntent().getIntExtra("transaction_id", -1);
        if (editTransactionId != -1) {
            isEditMode = true;
            getSupportActionBar().setTitle("Edit Transaction");
            loadTransaction(editTransactionId);
        } else {
            getSupportActionBar().setTitle("Add Transaction");
            tvDate.setText(DateUtils.formatDate(selectedDate));
        }

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupSpinners() {
        updateCategorySpinner(Constants.TYPE_EXPENSE);

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constants.CURRENCIES);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        String defCurrency = prefs.getString(Constants.PREF_BASE_CURRENCY, "MYR");
        for (int i = 0; i < Constants.CURRENCIES.length; i++) {
            if (Constants.CURRENCIES[i].equals(defCurrency)) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }
    }

    private void updateCategorySpinner(String type) {
        String[] cats = Constants.TYPE_EXPENSE.equals(type)
                ? Constants.EXPENSE_CATEGORIES
                : Constants.INCOME_CATEGORIES;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        tvDate.setText(DateUtils.formatDate(selectedDate));
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate);
            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar sel = Calendar.getInstance();
                sel.set(year, month, day);
                selectedDate = sel.getTimeInMillis();
                tvDate.setText(DateUtils.formatDate(selectedDate));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTypeToggle() {
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                updateCategorySpinner(Constants.TYPE_EXPENSE);
            } else {
                updateCategorySpinner(Constants.TYPE_INCOME);
            }
        });
    }

    private void loadTransaction(int id) {
        repository.getById(id, transaction -> {
            if (transaction == null) return;
            runOnUiThread(() -> {
                etTitle.setText(transaction.getTitle());
                etAmount.setText(String.valueOf(transaction.getAmount()));
                etNotes.setText(transaction.getNotes());
                selectedDate = transaction.getDate();
                tvDate.setText(DateUtils.formatDate(selectedDate));

                boolean isExpense = Constants.TYPE_EXPENSE.equals(transaction.getType());
                rgType.check(isExpense ? R.id.rb_expense : R.id.rb_income);
                updateCategorySpinner(transaction.getType());

                String[] cats = isExpense ? Constants.EXPENSE_CATEGORIES : Constants.INCOME_CATEGORIES;
                for (int i = 0; i < cats.length; i++) {
                    if (cats[i].equals(transaction.getCategory())) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }

                for (int i = 0; i < Constants.CURRENCIES.length; i++) {
                    if (Constants.CURRENCIES[i].equals(transaction.getCurrency())) {
                        spinnerCurrency.setSelection(i);
                        break;
                    }
                }
            });
        });
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (title.isEmpty()) { etTitle.setError("Title required"); return; }
        if (amountStr.isEmpty()) { etAmount.setError("Amount required"); return; }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }
        if (amount <= 0) { etAmount.setError("Amount must be > 0"); return; }

        String category = (String) spinnerCategory.getSelectedItem();
        String currency = Constants.CURRENCIES[spinnerCurrency.getSelectedItemPosition()];
        String type = rgType.getCheckedRadioButtonId() == R.id.rb_expense
                ? Constants.TYPE_EXPENSE : Constants.TYPE_INCOME;

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        String baseCurrency = prefs.getString(Constants.PREF_BASE_CURRENCY, "MYR");

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        currencyClient.convert(amount, currency, baseCurrency, new CurrencyApiClient.ConvertCallback() {
            @Override
            public void onSuccess(double convertedAmount, double rate, boolean isOffline) {
                completeSave(title, amount, category, type, currency, notes, selectedDate, convertedAmount, isOffline, false);
            }

            @Override
            public void onError(String error) {
                // If there's literally no cache at all, fallback to 1:1 original amount
                completeSave(title, amount, category, type, currency, notes, selectedDate, amount, true, true);
            }
        });
    }

    private void completeSave(String title, double amount, String category, String type,
                              String currency, String notes, long date, double converted, boolean isOffline, boolean isHardError) {
        Transaction t = new Transaction(title, amount, category, type,
                currency, notes, date, converted);

        if (isEditMode) {
            t.setId(editTransactionId);
            repository.update(t);
        } else {
            repository.insert(t, id -> {});
        }

        runOnUiThread(() -> {
            String message = "Saved!";
            if (isHardError) message = "Saved (Warning: No offline rates found. Used 1:1 conversion)";
            else if (isOffline) message = "Saved using last known rates (Offline)";

            Toast.makeText(AddEditExpenseActivity.this, message, Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}