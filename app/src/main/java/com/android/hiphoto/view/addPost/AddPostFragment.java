package com.android.hiphoto.view.addPost;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.hiphoto.R;
import com.android.hiphoto.models.Post;
import com.android.hiphoto.utils.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class AddPostFragment extends Fragment implements View.OnClickListener {

    private Button btnNext, btnAddImg;
    private ImageView imagePost;
    private EditText edtTitle;
    private Uri imageUri;
    private Bitmap selectedImage;
    private ProgressBar addImgProgressBar, addPostProgressBar;

    private static final int GALLERY_PERMISSION = 200;
    private static final int GALLERY_PICK = 100;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    private NavController navController;


    public AddPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);
        setUpView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void setUpView(View view)
    {
        btnNext = view.findViewById(R.id.addPost_button);
        imagePost = view.findViewById(R.id.addPost_imageView);
        edtTitle = view.findViewById(R.id.addPost_title_editText);
        btnAddImg = view.findViewById(R.id.add_img_btn);
        addPostProgressBar = view.findViewById(R.id.addPostProgressBar);
        addImgProgressBar = view.findViewById(R.id.addImgProgressBar);

        btnAddImg.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.add_img_btn:
                addImgProgressBar.setVisibility(View.VISIBLE);
                btnAddImg.setVisibility(View.INVISIBLE);
                checkAccessImagesPermission();

                break;
            case R.id.addPost_button:
                savePost();
                break;
        }
    }

    private void savePost(){

        try {

            addPostProgressBar.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.INVISIBLE);

            String title = edtTitle.getText().toString();
            String imgPath = UUID.randomUUID().toString() + ".jpg";
            mStorageRef.child("postImages").child(imgPath)
                    .putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                mStorageRef.child("postImages").child(imgPath).getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageURL = uri.toString();

                    Post post = new Post(title, imageURL);
//                    post.setImage(imageURL);
//                    post.setTitle(title);

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    assert currentUser != null;
                    post.setUserId(currentUser.getUid());
                    post.setDate(Utilities.getCurrentDate());
                    savePostToDB(post);
                });
            });

        }catch (Exception e){
            showMassage("Please Add Image First Or Click Home To Exit");

            addPostProgressBar.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.VISIBLE);
        }

    }


    private void savePostToDB(Post post){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts");
        String id = myRef.push().getKey();
        assert id != null;
        myRef.child(id).setValue(post);
        Toast.makeText(getActivity(), "Post Added!", Toast.LENGTH_SHORT).show();
        navController.popBackStack();

    }

    private void checkAccessImagesPermission(){
        int permission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
        }else {
            getImageFromGallery();
        }
    }

    private void getImageFromGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GALLERY_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getImageFromGallery();
            }else {
                Toast.makeText(getActivity(), "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){

            try {
                imageUri = data.getData();
                InputStream imageStream = requireActivity()
                        .getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);

                // base64
                // String imageBase64 = getResizedBase64(selectedImage, 100, 100);

                imagePost.setImageBitmap(selectedImage);
                btnAddImg.setText("Change Image");
                btnAddImg.setVisibility(View.VISIBLE);
                addImgProgressBar.setVisibility(View.INVISIBLE);

            }catch (FileNotFoundException e){
                e.printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();

                btnAddImg.setVisibility(View.VISIBLE);
                btnAddImg.setText("Click Her To Add Your Image");
                addImgProgressBar.setVisibility(View.INVISIBLE);
            }

        }else {
            Toast.makeText(getActivity(), "You haven't Picked Image", Toast.LENGTH_SHORT).show();

            btnAddImg.setVisibility(View.VISIBLE);
            btnAddImg.setText("Click Her To Add Your Image");
            addImgProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Use this method to convert and resize to base64
     *
     * @param bm        Bitmap
     * @param newWidth  int
     * @param newHeight int
     * @return String (base64)
     */
    private String getResizedBase64(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void showMassage(String massage){
        Toast.makeText(getContext(), massage, Toast.LENGTH_SHORT).show();
    }
}
