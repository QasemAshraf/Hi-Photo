package com.android.instanano.view.profile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.instanano.R;
import com.android.instanano.models.User;
import com.squareup.picasso.Picasso;

public class ProfileHolder extends RecyclerView.ViewHolder {

    private ImageView imgProfile;
    private TextView tvAccountName;
    private TextView tvEmail;


    public ProfileHolder(@NonNull View itemView) {
        super(itemView);
        setUpView();
    }

    private void setUpView(){

        imgProfile = itemView.findViewById(R.id.profile_image);
        tvAccountName = itemView.findViewById(R.id.profileName_TextView);
        tvEmail = itemView.findViewById(R.id.profileEmail_textView);

    }

    void bindView(User user){

        Picasso.get()
                .load(user.getImageAccount())
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(imgProfile);

        tvAccountName.setText(user.getNameOfAccount());
        tvEmail.setText(user.getEmail());

    }
}
