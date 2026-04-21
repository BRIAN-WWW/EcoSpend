package com.ecospend.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ecospend.app.R;
import com.ecospend.app.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.iv_splash_logo);
        TextView tagline = findViewById(R.id.tv_splash_tagline);
        TextView appName = findViewById(R.id.tv_splash_name);

        // Animate logo
        ScaleAnimation scale = new ScaleAnimation(0.5f, 1f, 0.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(600);
        scale.setFillAfter(true);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(600);
        fadeIn.setFillAfter(true);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.addAnimation(fadeIn);
        logo.startAnimation(set);

        AlphaAnimation textFade = new AlphaAnimation(0f, 1f);
        textFade.setDuration(800);
        textFade.setStartOffset(400);
        textFade.setFillAfter(true);
        appName.startAnimation(textFade);
        tagline.startAnimation(textFade);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 1800);
    }
}
