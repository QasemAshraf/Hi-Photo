package com.android.hiphoto.view.home.post;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hiphoto.R;
import com.android.hiphoto.models.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class PostHolder extends RecyclerView.ViewHolder {

    public ImageView imageLike;
    private ImageView imageAccount, imagePost;
    private TextView nameOfAccount, tvTitle, tvNumberOfLike, tvDate;

    public PostHolder(@NonNull View itemView) {
        super(itemView);
        setUpView();
    }

    private void setUpView(){

        imageAccount = itemView.findViewById(R.id.profile_image);
        nameOfAccount = itemView.findViewById(R.id.profileName_TextView);
        imagePost = itemView.findViewById(R.id.post_imageView);
        imageLike = itemView.findViewById(R.id.post_like_imageView);
        tvNumberOfLike = itemView.findViewById(R.id.post_numberOfLikes_textView);
        tvTitle = itemView.findViewById(R.id.post_title_textView);
        tvDate = itemView.findViewById(R.id.post_date_textView);

    }

    void bindView(Post post){

            Picasso.get()
                    .load(post.getUser().getImageAccount())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(imageAccount, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get()
                                    .load(post.getUser().getImageAccount())
                                    .placeholder(R.drawable.profile_placeholder)
                                    .error(R.drawable.profile_placeholder)
                                    .into(imageAccount);

                        }
                    });


        Picasso.get()
                .load(post.getImage())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(imagePost, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        Picasso.get()
                                .load(post.getImage())
                                .placeholder(R.drawable.img_placeholder)
                                .error(R.drawable.img_placeholder)
                                .into(imagePost);

                    }
                });

        nameOfAccount.setText(post.getUser().getNameOfAccount());
        tvTitle.setText(post.getTitle());
        tvDate.setText(post.getDate());
        String likes = post.getNumberOfLike() + " person";
        tvNumberOfLike.setText(likes);
    }
}
