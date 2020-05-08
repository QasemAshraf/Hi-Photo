package com.android.instanano.view.auth.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.instanano.R;
import com.android.instanano.utils.Utilities;
import com.android.instanano.view.auth.register.Register;
import com.android.instanano.view.main.MainActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView tvSignUp;

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpView();

    }


    private void setUpView(){
        edtEmail = findViewById(R.id.login_email_EditText);
        edtPassword = findViewById(R.id.login_password_EditText);
        btnLogin = findViewById(R.id.login_button);
        tvSignUp = findViewById(R.id.loginSignUpTextView);

        btnLogin.setOnClickListener(this);

        tvSignUp.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.login_button:
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                //TODO: Validation
                signIn(email, password);
//                startActivity(new Intent(Login.this, MainActivity.class));
                break;
            case R.id.loginSignUpTextView:
                startActivity(new Intent(Login.this, Register.class));
                break;
        }
    }


    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        logUserEvent(email);
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Login.this, "wrong email and password",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logUserEvent(String email){
        Bundle bundle = new Bundle();
        bundle.putString("user_email", email);
        bundle.putString("user_login_date", Utilities.getCurrentDate());
        bundle.putString("type", "login");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

    }

}
