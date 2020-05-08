package com.android.instanano.view.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.instanano.R;
import com.android.instanano.models.User;
import com.android.instanano.view.home.HomeFragment;
import com.android.instanano.view.home.post.PostAdapter;
import com.android.instanano.view.splash.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private ImageView imageAccount;
    private TextView nameOfAccount;
    private TextView tvEmail;
    private Button btnLogOut;
    private Button btnEditPhoto;

    private ProfileAdapter profileAdapter;
    private ArrayList<User> users;
    private User user;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;


    public ProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setUpView(view);
        getUser();
        return view;
    }


    private void setUpView(View view) {

        recyclerView = view.findViewById(R.id.profile_recyclerView);

        imageAccount = view.findViewById(R.id.profile_image);
        nameOfAccount = view.findViewById(R.id.profileName_TextView);
        tvEmail = view.findViewById(R.id.profileEmail_textView);
        btnLogOut = view.findViewById(R.id.profileLogOutButton);
        btnEditPhoto = view.findViewById(R.id.profileEitPhotoButton);

        users = new ArrayList<>();

        btnEditPhoto.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Photos");

        profileAdapter = new ProfileAdapter(users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(profileAdapter);



    }

    private void getUser(){

        myRef = database.getReference("Users").child(firebaseUser.getUid());
        myRef = database.getReference("Users").child(user.getId());


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                user = dataSnapshot.getValue(User.class);
                user.setId(id);

                nameOfAccount.setText(user.getNameOfAccount());
                tvEmail.setText(user.getEmail());
                users.add(user);
                profileAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getPhoto(){

        myRef = database.getReference("Photos").child(firebaseUser.getUid());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                nameOfAccount.setText(user.getNameOfAccount());
                tvEmail.setText(user.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profileLogOutButton:
                mAuth.signOut();
                requireActivity()
                        .startActivity(new Intent(getActivity(), SplashActivity.class));
                break;
            case R.id.profileEitPhotoButton:
                requireActivity()
                        .startActivity(new Intent(getActivity(), EditProfile.class));
                break;
        }

    }

}
