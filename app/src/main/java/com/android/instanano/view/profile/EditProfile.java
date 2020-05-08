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
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class EditProfile extends Fragment implements View.OnClickListener {

    private ImageView imgProfile;
    private EditText editName;
    private EditText editEmail;
    private Button btnEditPhoto;
    private Button btnSave;

    private Uri imageUri;
    private Bitmap selectedImage;

    private NavController navController;

    private static final int Gallery_Permission = 200;
    private static final int Gallery_Pick = 100;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile, container, false);
        setUpView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        checkAccessImagePermission();
    }

    private void setUpView(View view) {
        imgProfile = view.findViewById(R.id.profile_image);
        editName = view.findViewById(R.id.profileNameEditText);
        editEmail = view.findViewById(R.id.profileEmailEditText);
        btnEditPhoto = view.findViewById(R.id.profileEitPhotoButton);
        btnSave = view.findViewById(R.id.profileSaveButton);

//        btnEditPhoto.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }



    private void savePhoto() {

        String name = editName.toString();
        String email = editEmail.toString();

        String imagePath = UUID.randomUUID().toString() + ".jpg";
        mStorageRef.child("photoImages").child(imagePath)
                .putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            mStorageRef.child("photoImages").child(imagePath)
                    .getDownloadUrl().addOnSuccessListener(uri -> {
                String imageURL = uri.toString();

                User user = new User();
                user.setImageAccount(imageURL);
                user.setNameOfAccount(name);
                user.setEmail(email);

                FirebaseUser currentUser = mAuth.getCurrentUser();
                user.setId(currentUser.getUid());
                savePhotoToDB(user);

            });
        });
    }

    private void savePhotoToDB(User user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Photos");
        String id = myRef.push().getKey();
        assert id != null;
        myRef.child(id).setValue(user);
        Toast.makeText(getActivity(), "Photo Added!", Toast.LENGTH_SHORT).show();

    }


    private void checkAccessImagePermission() {
        int permission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getActivity(), "You haven't Picked Image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.profileSaveButton:
                savePhoto();
                break;
//            case R.id.profileEitPhotoButton:
//                checkAccessImagePermission();
//                break;
        }
    }

    }

