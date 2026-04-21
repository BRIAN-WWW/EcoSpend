package com.ecospend.app.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ecospend.app.R;
import com.ecospend.app.activities.fragments.CurrencyFragment;
import com.ecospend.app.activities.fragments.HomeFragment;
import com.ecospend.app.activities.fragments.ProfileFragment;
import com.ecospend.app.activities.fragments.SummaryFragment;
import com.ecospend.app.activities.fragments.TransactionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fab;

    private HomeFragment homeFragment;
    private TransactionFragment transactionFragment;
    private SummaryFragment summaryFragment;
    private CurrencyFragment currencyFragment;
    private ProfileFragment profileFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fab_add);

        initFragments();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(homeFragment);
                fab.show();
                return true;
            } else if (id == R.id.nav_transactions) {
                showFragment(transactionFragment);
                fab.show();
                return true;
            } else if (id == R.id.nav_summary) {
                showFragment(summaryFragment);
                fab.hide();
                return true;
            } else if (id == R.id.nav_currency) {
                showFragment(currencyFragment);
                fab.hide();
                return true;
            } else if (id == R.id.nav_profile) {
                showFragment(profileFragment);
                fab.hide();
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddEditExpenseActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        });
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        transactionFragment = new TransactionFragment();
        summaryFragment = new SummaryFragment();
        currencyFragment = new CurrencyFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragment_container, currencyFragment, "currency").hide(currencyFragment)
                .add(R.id.fragment_container, summaryFragment, "summary").hide(summaryFragment)
                .add(R.id.fragment_container, transactionFragment, "transactions").hide(transactionFragment)
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();

        activeFragment = homeFragment;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit();
        activeFragment = fragment;
    }
}
