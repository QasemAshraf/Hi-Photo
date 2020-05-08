package com.android.instanano.view.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.instanano.R;
import com.android.instanano.view.auth.login.Login;
import com.android.instanano.view.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        new Handler().postDelayed(() -> {
            if (currentUser == null){
                startActivity(new Intent(SplashActivity.this, Login.class));
            }else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        },2000);
    }
}
