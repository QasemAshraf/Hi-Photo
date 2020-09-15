package com.android.instanano.view.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.android.instanano.R;
import com.android.instanano.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment implements View.OnClickListener {

    private ImageView imgProfile;
    private EditText editName, editEmail;
    private Button btnEditPhoto, btnSave;
    private ProgressBar profileSaveProgressBar, profileEditProgressBar;

    private NavController navController;

    private Uri imageUri;
    private Bitmap selectedImage;


    private static final int Gallery_Permission = 200;
    private static final int Gallery_Pick = 100;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    public EditProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        setUpView(view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        setUpView(view);
    }

    private void setUpView(View view)
    {
        imgProfile = view.findViewById(R.id.profile_image);
        editName = view.findViewById(R.id.profileNameEditText);
        editEmail = view.findViewById(R.id.profileEmailEditText);
        btnEditPhoto = view.findViewById(R.id.profileEditButton);
        btnSave = view.findViewById(R.id.profileSaveButton);
        profileEditProgressBar = view.findViewById(R.id.profileEditProgressBar);
        profileSaveProgressBar = view.findViewById(R.id.profileSaveProgressBar);

        btnEditPhoto.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    private void savePhoto()
    {

        try {

            profileSaveProgressBar.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.INVISIBLE);

            User user = new User();

            String name = editName.getText().toString();
            String email = editEmail.getText().toString();

                mStorageRef = FirebaseStorage.getInstance().getReference();
                mAuth = FirebaseAuth.getInstance();

                String imagePath = UUID.randomUUID().toString() + ".jpg";
                mStorageRef.child("profilePhoto").child(imagePath)
                        .putFile(imageUri).addOnSuccessListener(taskSnapshot -> mStorageRef.child("profilePhoto").child(imagePath)
                                .getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageURL = uri.toString();
                            user.setImageAccount(imageURL);
                            user.setNameOfAccount(name);
                            user.setEmail(email);

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(Uri.parse(imageURL))
                            .build();

                            firebaseUser.updateProfile(profileUpdate);

                            if (name.isEmpty())
                            {
                                user.setNameOfAccount(firebaseUser.getDisplayName());

                            }if (email.isEmpty())
                            {
                                user.setEmail(firebaseUser.getEmail());

                            }if (user.getImageAccount().isEmpty())
                            {
                                Picasso.get()
                                        .load(firebaseUser.getPhotoUrl())
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .error(R.drawable.profile_placeholder)
                                        .placeholder(R.drawable.profile_placeholder)
                                        .into(imgProfile);
                            }
                            savePhotoToDB(firebaseUser.getUid(), user);
                        }));

        } catch (Exception e) {
            showMassage("Please Add Photo First Or Click Home To Exit");
            profileSaveProgressBar.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.VISIBLE);
        }

    }

    private void savePhotoToDB(String id, User user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(id);
        myRef.setValue(user);
        Toast.makeText(getActivity(), "Photo Added!", Toast.LENGTH_SHORT).show();
        navController.popBackStack();

    }


    private void checkAccessImagePermission() {
        int permission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Gallery_Permission);
        } else {
            getPhotoFromGallery();
        }
    }

    private void getPhotoFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Gallery_Pick);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Gallery_Permission) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhotoFromGallery();
            } else {
                Toast.makeText(getActivity(), "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            try {
                assert data != null;
                imageUri = data.getData();
                InputStream inputStream = requireActivity()
                        .getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(inputStream);
                imgProfile.setImageBitmap(selectedImage);

                profileEditProgressBar.setVisibility(View.INVISIBLE);
                btnEditPhoto.setVisibility(View.VISIBLE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();

                profileEditProgressBar.setVisibility(View.INVISIBLE);
                btnEditPhoto.setVisibility(View.VISIBLE);
            }

        } else {
            Toast.makeText(getActivity(), "You haven't Picked Image", Toast.LENGTH_SHORT).show();
            btnEditPhoto.setVisibility(View.VISIBLE);
            profileEditProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.profileSaveButton:
                savePhoto();
                break;
            case R.id.profileEditButton:
                profileEditProgressBar.setVisibility(View.VISIBLE);
                checkAccessImagePermission();
                break;
        }
    }

    private void showMassage(String massage) {
        Toast.makeText(getContext(), massage, Toast.LENGTH_SHORT).show();
    }
}

