package com.android.instanano.view.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.instanano.R;
import com.android.instanano.models.User;
import com.android.instanano.utils.Utilities;
import com.android.instanano.view.auth.register.Register;
import com.android.instanano.view.main.MainActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnSignInSignOut;
    private ProgressBar loginProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpView();

    }


    private void setUpView()
    {
        edtEmail = findViewById(R.id.login_email_EditText);
        edtPassword = findViewById(R.id.login_password_EditText);
        btnLogin = findViewById(R.id.login_button);
        btnSignInSignOut = findViewById(R.id.loginSignUpBtn);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        btnLogin.setOnClickListener(this);
        btnSignInSignOut.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onClick(View v)
    {

        switch (v.getId()){

            case R.id.loginSignUpBtn:
                startActivity(new Intent(Login.this, Register.class));
                break;

            case R.id.login_button:

                loginProgressBar.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                if (email.isEmpty())
                {
                    edtEmail.setError("Input Email");
                    loginProgressBar.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                else if (password.isEmpty())
                {
                    edtPassword.setError("Input Password");
                    loginProgressBar.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
                else
                {
                    //TODO: Validation
                    User user = new User(email, password);
                    signIn(user);
                }
                    break;
        }
    }


    private void signIn(User user)
    {
        mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        logUserEvent(user.getEmail());
                        updateUI();
                        finish();
                    }
                    else
                     {
                        loginProgressBar.setVisibility(View.INVISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                        showMessage(Objects.requireNonNull(task.getException()).getMessage());
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

    private void updateUI() {

        startActivity(new Intent(Login.this, MainActivity.class));

    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
