package com.avinash.syncopyproject.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.R;

import java.util.ArrayList;

public class EditTextAdapter extends RecyclerView.Adapter<EditTextAdapter.ViewHolder> {

    private static final String TAG = "EditTextAdapter";
    private ArrayList<Integer> images;

    public EditTextAdapter(ArrayList<Integer> images) {
        Log.i(TAG, "EditTextAdapter: GOT THE VALUES");
        this.images = images;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.i(TAG, "ViewHolder: INFLATING THE IMAGE");
            image = itemView.findViewById(R.id.edit_profile_image);
        }
    }

    @NonNull
    @Override
    public EditTextAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: START");
        ImageView view = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_profile_container, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditTextAdapter.ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: START");
        holder.image.setImageResource(images.get(position));
    
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


}
