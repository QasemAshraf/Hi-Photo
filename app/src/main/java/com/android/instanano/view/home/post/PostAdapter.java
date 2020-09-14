package com.android.instanano.view.home.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.instanano.R;
import com.android.instanano.models.Post;
import com.android.instanano.utils.OnLikedClicked;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostHolder> {

    private List<Post> posts;
    private OnLikedClicked onLikedClicked;

    public PostAdapter(List<Post> posts, OnLikedClicked onLikedClicked) {
        this.posts = posts;
        this.onLikedClicked = onLikedClicked;
    }


    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        holder.bindView(posts.get(position));

        holder.imageLike.setOnClickListener(v -> {
            onLikedClicked.onLikedClicked(position);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}