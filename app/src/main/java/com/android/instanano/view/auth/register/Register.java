package com.android.instanano.view.auth.register;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.instanano.R;
import com.android.instanano.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText nameOfAccount;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpView();

    }

    private void setUpView(){

        nameOfAccount = findViewById(R.id.profileName_TextView);
        edtEmail = findViewById(R.id.register_yourEmail_editText);
        edtPassword = findViewById(R.id.register_yourPassword_editText);
        btnRegister = findViewById(R.id.register_button);

        btnRegister.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {

        String name = nameOfAccount.getText().toString();
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        //TODO:Validation
        User user = new User();
        user.setNameOfAccount(name);
        user.setEmail(email);
        user.setPassword(password);
        createUser(user);
    }

    private void createUser(@NonNull User user){
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        assert firebaseUser != null;
                        saveUserToDB(firebaseUser.getUid(), user);
                    } else {
                        Toast.makeText(Register.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDB(String id, @NonNull User user){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(id);
        user.setPassword(null);
        myRef.setValue(user);

        Toast.makeText(Register.this, "success!",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
