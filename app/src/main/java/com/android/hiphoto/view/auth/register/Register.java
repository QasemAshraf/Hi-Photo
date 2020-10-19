package com.android.hiphoto.view.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.hiphoto.R;
import com.android.hiphoto.models.User;
import com.android.hiphoto.view.main.MainActivity;
import com.android.hiphoto.view.auth.login.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText nameOfAccount, edtEmail, edtPassword, edtPassword2;
    private Button btnRegister, btnLogin;
    private ProgressBar regProgressBar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpView();

    }

    private void setUpView()
    {

        nameOfAccount = findViewById(R.id.profileName_TextView);
        edtEmail = findViewById(R.id.register_yourEmail_editText);
        edtPassword = findViewById(R.id.register_yourPassword_editText);
        edtPassword2 = findViewById(R.id.register_yourPassword2_editText);
        btnRegister = findViewById(R.id.register_button);
        btnLogin = findViewById(R.id.goToLogin_btn);
        regProgressBar = findViewById(R.id.regProgressBar);

        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }


    public boolean validateName()
    {
        String useName = nameOfAccount.getText().toString();
        String noWhiteSpace = "(?=\\s+%)";
        if (useName.isEmpty()){
            nameOfAccount.setError("Please Input Your Name");
            return false;
        }else if (useName.length() >= 15){
            nameOfAccount.setError("User Name To Long");
            return false;
        }else if (useName.matches(noWhiteSpace)){
            nameOfAccount.setError("White Space Is Not Allowed");
            return false;
        }else {
            return true;
        }

    }

    public boolean validateEmail()
    {

        String email = edtEmail.getText().toString();
        String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        if (email.matches(EMAIL_PATTERN))
        {
            return true;
        }
        else
        {
            edtEmail.setError("Invalid Email");
            return false;
        }
    }

    public boolean validatePassword()
    {
        String pass1 = edtPassword.getText().toString();
        String pass2 = edtPassword2.getText().toString();
        if (!(pass1.length() > 5)){
            edtPassword.setError("Password Should'n be less than 6 ");
            return false;
        }else if (!(pass1.equals(pass2))){
            edtPassword2.setError("Confirm Password must be equal Password ");
            return false;
        }else {
            return true;
        }
    }

    private void validateData()
    {
        String name = nameOfAccount.getText().toString();
        String email = edtEmail.getText().toString();
        String pass1 = edtPassword.getText().toString();
        String pass2 = edtPassword2.getText().toString();

        if (!validateName() | !validateEmail() | !validatePassword())
        {
            btnRegister.setVisibility(View.VISIBLE);
            regProgressBar.setVisibility(View.INVISIBLE);
        }
        else
         {
           btnRegister.setVisibility(View.INVISIBLE);
           regProgressBar.setVisibility(View.VISIBLE);

            //TODO:Validation
            User user = new User(name, email, pass1);
            createUser(user);
        }
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.register_button:
                btnLogin.setVisibility(View.INVISIBLE);
                validateData();
            break;

            case R.id.goToLogin_btn:
                startActivity(new Intent(Register.this, Login.class));

            break;
        }

    }


    private void createUser(@NonNull User user){
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user.getNameOfAccount())
                                .build();

                        firebaseUser.updateProfile(profileUpdates);

                        saveUserToDB(firebaseUser.getUid(), user);
                        user.setId(firebaseUser.getUid());
                        updateUI();


                    } else {
                        showMessage("Authentication failed.");
                        btnRegister.setVisibility(View.VISIBLE);
                        regProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void saveUserToDB(String id, @NonNull User user){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(id);
        user.setPassword(null);
        myRef.setValue(user);
    }

    private void updateUI() {
        startActivity(new Intent(Register.this, MainActivity.class));
        finish();
    }

    // Simple Method To Show Toast Message
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
