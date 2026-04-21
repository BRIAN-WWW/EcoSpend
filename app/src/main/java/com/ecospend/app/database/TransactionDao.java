package com.ecospend.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ecospend.app.models.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);

    @Query("SELECT * FROM transactions WHERE type = 'expense' ORDER BY date DESC")
    LiveData<List<Transaction>> getAllExpenses();

    @Query("SELECT * FROM transactions WHERE type = 'income' ORDER BY date DESC")
    LiveData<List<Transaction>> getAllIncome();

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate);

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    @Query("SELECT SUM(amountInMYR) FROM transactions WHERE type = 'expense' AND date >= :startDate AND date <= :endDate")
    double getTotalExpensesInRange(long startDate, long endDate);

    @Query("SELECT SUM(amountInMYR) FROM transactions WHERE type = 'income' AND date >= :startDate AND date <= :endDate")
    double getTotalIncomeInRange(long startDate, long endDate);

    @Query("SELECT category, SUM(amountInMYR) as total FROM transactions WHERE type = 'expense' AND date >= :startDate AND date <= :endDate GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> getCategoryTotals(long startDate, long endDate);

    @Query("DELETE FROM transactions")
    void deleteAll();
}
