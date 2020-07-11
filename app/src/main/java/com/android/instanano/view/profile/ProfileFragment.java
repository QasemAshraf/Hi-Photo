package com.android.instanano.view.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import com.android.instanano.models.Post;
import com.android.instanano.models.User;
import com.android.instanano.utils.OnLikedClicked;
import com.android.instanano.view.home.post.PostAdapter;
import com.android.instanano.view.splash.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProfileFragment extends Fragment implements View.OnClickListener, OnLikedClicked {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> posts;

    private ImageView imageAccount;
    private TextView nameOfAccount;
    private TextView tvEmail;


    private Button btnLogOut;
    private Button btnEdit;
    private String userId;

    private NavController navController;
    private DatabaseReference myRefUser;
    private FirebaseDatabase database;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;


    public ProfileFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setUpView(view);
        getMyPosts();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void setUpView(View view) {

        recyclerView = view.findViewById(R.id.profile_recyclerView);

        imageAccount = view.findViewById(R.id.fragment_profile_image);
        nameOfAccount = view.findViewById(R.id.fragment_profileName_TextView);
        tvEmail = view.findViewById(R.id.fragment_profileEmail_textView);
        btnLogOut = view.findViewById(R.id.fragment_profile_logOutButton);
        btnEdit = view.findViewById(R.id.fragment_profile_editButton);

        btnEdit.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, ProfileFragment.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(postAdapter);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        myRefUser = database.getReference("Users").child(firebaseUser.getUid());

        myRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                nameOfAccount.setText(user.getNameOfAccount());
                tvEmail.setText(user.getEmail());
                userId = user.getId();
                Picasso.get()
                        .load(user.getImageAccount())
                        .placeholder(R.drawable.img_placeholder)
                        .into(imageAccount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_profile_logOutButton:
                mAuth.signOut();
                requireActivity()
                        .startActivity(new Intent(getActivity(), SplashActivity.class));
                break;
            case R.id.fragment_profile_editButton:
                navController.navigate(R.id.action_profileFragment_to_editProfile);
                break;
        }

    }

    private void getMyPosts(){

        Query query = database.getReference().child("Posts")
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    final Post post = snapshot.getValue(Post.class);
                    post.setId(id);

                    DatabaseReference usersRef = database.getReference("Users").child(post.getUserId());

                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            post.setUser(user);
                            posts.add(post);
                            postAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikedClicked(int position) {

        Post post = posts.get(position);
        DatabaseReference myRef = database.getReference("Posts")
                .child(post.getId()).child("numberOfLike");
        DatabaseReference likeRef = database.getReference("UserLikes")
                .child(firebaseUser.getUid()).child(post.getId()).child("didLike");
        final int numberOfLike = post.getNumberOfLike();
        myRef.setValue(numberOfLike);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean didLike = dataSnapshot.getValue(Boolean.class);
                if (didLike != null && didLike) {

                    myRef.setValue(numberOfLike - 1);
                    posts.get(position).setNumberOfLike(numberOfLike - 1);
                    likeRef.setValue(false);

                } else {
                    myRef.setValue(numberOfLike + 1);
                    posts.get(position).setNumberOfLike(numberOfLike + 1);
                    likeRef.setValue(true);

                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}