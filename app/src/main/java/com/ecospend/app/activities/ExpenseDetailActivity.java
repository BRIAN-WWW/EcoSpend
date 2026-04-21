package com.ecospend.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ecospend.app.R;
import com.ecospend.app.database.TransactionRepository;
import com.ecospend.app.models.Transaction;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.CurrencyFormatter;
import com.ecospend.app.utils.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExpenseDetailActivity extends AppCompatActivity {

    private TransactionRepository repository;
    private Transaction currentTransaction;

    private View colorBadge;
    private ImageView ivIcon;
    private TextView tvTitle, tvAmount, tvCategory, tvType, tvDate, tvCurrency, tvNotes, tvMyrEquiv;
    private FloatingActionButton fabEdit;
    private View btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction Details");
        }

        colorBadge = findViewById(R.id.view_detail_color);
        ivIcon = findViewById(R.id.iv_detail_icon);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvAmount = findViewById(R.id.tv_detail_amount);
        tvCategory = findViewById(R.id.tv_detail_category);
        tvType = findViewById(R.id.tv_detail_type);
        tvDate = findViewById(R.id.tv_detail_date);
        tvCurrency = findViewById(R.id.tv_detail_currency);
        tvNotes = findViewById(R.id.tv_detail_notes);
        tvMyrEquiv = findViewById(R.id.tv_detail_myr_equiv);
        fabEdit = findViewById(R.id.fab_edit);
        btnDelete = findViewById(R.id.btn_delete_transaction);

        repository = new TransactionRepository(getApplication());

        int transactionId = getIntent().getIntExtra("transaction_id", -1);
        if (transactionId == -1) { finish(); return; }

        repository.getById(transactionId, t -> {
            if (t == null) { finish(); return; }
            currentTransaction = t;
            runOnUiThread(() -> bindData(t));
        });
    }

    private void bindData(Transaction t) {
        int color = Constants.getCategoryColor(t.getCategory());
        colorBadge.setBackgroundColor(color);
        ivIcon.setImageResource(Constants.getCategoryIcon(t.getCategory()));
        ivIcon.setColorFilter(color);

        tvTitle.setText(t.getTitle());

        boolean isExpense = Constants.TYPE_EXPENSE.equals(t.getType());
        String prefix = isExpense ? "- " : "+ ";
        tvAmount.setText(prefix + CurrencyFormatter.format(t.getAmount(), t.getCurrency()));
        tvAmount.setTextColor(getResources().getColor(
                isExpense ? R.color.expense_red : R.color.income_green, null));

        tvCategory.setText(t.getCategory());
        tvType.setText(isExpense ? "Expense" : "Income");
        tvDate.setText(DateUtils.formatDate(t.getDate()));
        tvCurrency.setText(t.getCurrency());

        if (!t.getCurrency().equals("MYR")) {
            tvMyrEquiv.setVisibility(View.VISIBLE);
            tvMyrEquiv.setText("≈ " + CurrencyFormatter.format(t.getAmountInMYR(), "MYR"));
        } else {
            tvMyrEquiv.setVisibility(View.GONE);
        }

        if (t.getNotes() != null && !t.getNotes().isEmpty()) {
            tvNotes.setText(t.getNotes());
            tvNotes.setVisibility(View.VISIBLE);
        } else {
            tvNotes.setVisibility(View.GONE);
        }

        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditExpenseActivity.class);
            intent.putExtra("transaction_id", t.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        });

        btnDelete.setOnClickListener(v -> confirmDelete(t));
    }

    private void confirmDelete(Transaction t) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete \"" + t.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    repository.delete(t);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTransaction != null) {
            repository.getById(currentTransaction.getId(), t -> {
                if (t != null) {
                    currentTransaction = t;
                    runOnUiThread(() -> bindData(t));
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
