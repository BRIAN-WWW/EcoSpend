package com.ecospend.app.activities.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.ecospend.app.R;
import com.ecospend.app.database.CategoryTotal;
import com.ecospend.app.database.TransactionRepository;
import com.ecospend.app.utils.Constants;
import com.ecospend.app.utils.CurrencyFormatter;
import com.ecospend.app.utils.DateUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SummaryFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvMonthExpense, tvMonthIncome, tvMonthNet, tvSummaryMonth;
    private TransactionRepository repository;
    private String baseCurrency = "MYR";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        tvMonthExpense = view.findViewById(R.id.tv_month_expense);
        tvMonthIncome = view.findViewById(R.id.tv_month_income);
        tvMonthNet = view.findViewById(R.id.tv_month_net);
        tvSummaryMonth = view.findViewById(R.id.tv_summary_month);

        repository = new TransactionRepository(requireActivity().getApplication());

        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        baseCurrency = prefs.getString(Constants.PREF_BASE_CURRENCY, "MYR");

        tvSummaryMonth.setText(DateUtils.formatMonthYear(System.currentTimeMillis()));

        setupPieChart();
        setupBarChart();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(11f);
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.setFitBars(true);
        barChart.animateY(800);
    }

    private void loadData() {
        int year = DateUtils.getCurrentYear();
        int month = DateUtils.getCurrentMonth();
        long start = DateUtils.getStartOfMonth(year, month);
        long end = DateUtils.getEndOfMonth(year, month);

        // Load current month totals + category breakdown
        repository.getTotalsInRange(start, end, (expenses, income, cats) -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                tvMonthExpense.setText(CurrencyFormatter.format(expenses, baseCurrency));
                tvMonthIncome.setText(CurrencyFormatter.format(income, baseCurrency));
                double net = income - expenses;
                tvMonthNet.setText((net >= 0 ? "+" : "") + CurrencyFormatter.format(net, baseCurrency));
                tvMonthNet.setTextColor(net >= 0
                        ? getResources().getColor(R.color.income_green, null)
                        : getResources().getColor(R.color.expense_red, null));

                updatePieChart(cats);
            });
        });

        // Load 6-month bar chart data
        loadBarData(year, month);
    }

    private void updatePieChart(List<CategoryTotal> cats) {
        if (cats == null || cats.isEmpty()) {
            pieChart.setNoDataText("No expenses this month");
            pieChart.setNoDataTextColor(Color.WHITE);
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < cats.size(); i++) {
            CategoryTotal ct = cats.get(i);
            if (ct.total > 0) {
                entries.add(new PieEntry((float) ct.total, ct.category));
                colors.add(Constants.getCategoryColor(ct.category));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    private void loadBarData(int currentYear, int currentMonth) {
        List<String> labels = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();
        final int[] loaded = {0};
        final float[] monthTotals = new float[6];

        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        for (int i = 5; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.set(currentYear, currentMonth, 1);
            cal.add(Calendar.MONTH, -i);
            int yr = cal.get(Calendar.YEAR);
            int mo = cal.get(Calendar.MONTH);
            final int idx = 5 - i;
            labels.add(monthNames[mo]);

            long s = DateUtils.getStartOfMonth(yr, mo);
            long e = DateUtils.getEndOfMonth(yr, mo);

            repository.getTotalsInRange(s, e, (exp, inc, c) -> {
                monthTotals[idx] = (float) exp;
                loaded[0]++;
                if (loaded[0] == 6) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        for (int j = 0; j < 6; j++) {
                            entries.add(new BarEntry(j, monthTotals[j]));
                        }
                        BarDataSet dataSet = new BarDataSet(entries, "Monthly Expenses");
                        dataSet.setColor(0xFF4ECDC4);
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setValueTextSize(9f);
                        BarData barData = new BarData(dataSet);
                        barData.setBarWidth(0.7f);
                        barChart.setData(barData);
                        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                        barChart.invalidate();
                    });
                }
            });
        }
    }
}
