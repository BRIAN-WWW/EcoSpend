package com.ecospend.app.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ecospend.app.models.Transaction;
import com.ecospend.app.models.UserProfile;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final UserProfileDao userProfileDao;
    private final ExecutorService executor;
    private final LiveData<List<Transaction>> allTransactions;

    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        userProfileDao = db.userProfileDao();
        executor = Executors.newFixedThreadPool(4);
        allTransactions = transactionDao.getAllTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getExpenses() {
        return transactionDao.getAllExpenses();
    }

    public LiveData<List<Transaction>> getTransactionsByDateRange(long start, long end) {
        return transactionDao.getTransactionsByDateRange(start, end);
    }

    public void insert(Transaction transaction, InsertCallback callback) {
        executor.execute(() -> {
            long id = transactionDao.insert(transaction);
            if (callback != null) callback.onInserted((int) id);
        });
    }

    public void update(Transaction transaction) {
        executor.execute(() -> transactionDao.update(transaction));
    }

    public void delete(Transaction transaction) {
        executor.execute(() -> transactionDao.delete(transaction));
    }

    public void getById(int id, SingleCallback callback) {
        executor.execute(() -> {
            Transaction t = transactionDao.getTransactionById(id);
            if (callback != null) callback.onResult(t);
        });
    }

    public void getTotalsInRange(long start, long end, TotalsCallback callback) {
        executor.execute(() -> {
            double expenses = transactionDao.getTotalExpensesInRange(start, end);
            double income = transactionDao.getTotalIncomeInRange(start, end);
            List<CategoryTotal> cats = transactionDao.getCategoryTotals(start, end);
            if (callback != null) callback.onResult(expenses, income, cats);
        });
    }

    public void getAllSync(SyncListCallback callback) {
        executor.execute(() -> {
            List<Transaction> list = transactionDao.getAllTransactionsSync();
            if (callback != null) callback.onResult(list);
        });
    }

    // Profile
    public void saveProfile(UserProfile profile) {
        executor.execute(() -> userProfileDao.insert(profile));
    }

    public void getProfile(ProfileCallback callback) {
        executor.execute(() -> {
            UserProfile p = userProfileDao.getProfile();
            if (callback != null) callback.onResult(p);
        });
    }

    public interface InsertCallback { void onInserted(int id); }
    public interface SingleCallback { void onResult(Transaction t); }
    public interface TotalsCallback { void onResult(double expenses, double income, List<CategoryTotal> cats); }
    public interface SyncListCallback { void onResult(List<Transaction> transactions); }
    public interface ProfileCallback { void onResult(UserProfile profile); }
}
