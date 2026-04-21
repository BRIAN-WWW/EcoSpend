package com.ecospend.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ecospend.app.R;
import com.ecospend.app.models.Transaction;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.CurrencyFormatter;
import com.ecospend.app.utils.DateUtils;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    private OnItemClickListener listener;

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Transaction>() {
                @Override
                public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.getId() == newItem.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle())
                            && oldItem.getAmount() == newItem.getAmount()
                            && oldItem.getDate() == newItem.getDate();
                }
            };

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View categoryColorBar;
        private final ImageView ivCategoryIcon;
        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final TextView tvCurrency;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryColorBar = itemView.findViewById(R.id.view_category_color);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvTitle = itemView.findViewById(R.id.tv_transaction_title);
            tvCategory = itemView.findViewById(R.id.tv_transaction_category);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvCurrency = itemView.findViewById(R.id.tv_transaction_currency);
        }

        void bind(Transaction t) {
            Context ctx = itemView.getContext();
            tvTitle.setText(t.getTitle());
            tvCategory.setText(t.getCategory());
            tvDate.setText(DateUtils.getRelativeDate(t.getDate()));

            int color = Constants.getCategoryColor(t.getCategory());
            categoryColorBar.setBackgroundColor(color);

            int iconRes = Constants.getCategoryIcon(t.getCategory());
            ivCategoryIcon.setImageResource(iconRes);
            ivCategoryIcon.setColorFilter(color);

            boolean isExpense = Constants.TYPE_EXPENSE.equals(t.getType());
            String prefix = isExpense ? "- " : "+ ";
            tvAmount.setText(prefix + CurrencyFormatter.format(t.getAmount(), t.getCurrency()));
            tvAmount.setTextColor(ContextCompat.getColor(ctx,
                    isExpense ? R.color.expense_red : R.color.income_green));
            tvCurrency.setText(t.getCurrency());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(t);
            });
        }
    }
}
