package com.avinash.syncopyproject.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.CustomBottomSheet;
import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.Model.User;
import com.avinash.syncopyproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {

    public static final String SELECTIVE_CONTACTS = "selective contacts";

    private Set<String> selective_set = new HashSet<>();
    private SharedPreferences sharedPreferences;
    private ArrayList<String> useless = new ArrayList<>();
    private static final String TAG = "BottomSheetAdapter";
    private Context mContext;
    private ArrayList<String> connections;
    private View view;
    private FirebaseAuth mAuth;
    private ClipboardManager clipboard;

    public BottomSheetAdapter(Context context, ArrayList<String> connections) {
        mContext = context;
        this.connections = connections;
        mAuth = FirebaseAuth.getInstance();
//        selective_set = new HashSet<>(connections);

        try{
            sharedPreferences = mContext.getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE);
            selective_set = sharedPreferences.getStringSet(SELECTIVE_CONTACTS, new HashSet<>(useless));
            selective_set.add(mAuth.getCurrentUser().getUid());
        } catch (Exception e){
            //Do something
        }

        try {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        catch (NullPointerException e){
            //Do something
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_container, parent, false);
        return new BottomSheetAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        fillUserDetails(connections.get(position), holder.profileI, holder.useridT, holder.usernameT);

        if(position % 2 == 0){
            holder.constrain.setBackgroundColor(mContext.getResources().getColor(R.color.card_color_dark));
        }
        else{
            holder.constrain.setBackgroundColor(mContext.getResources().getColor(R.color.card_color_light));
        }

        holder.constrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked())
                    holder.checkBox.setChecked(false);
                else
                    holder.checkBox.setChecked(true);
            }
        });

        if(selective_set.contains(connections.get(position)))
            holder.checkBox.setChecked(true);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    selective_set.add(connections.get(position));
                    sharedPreferences.edit().putStringSet(SELECTIVE_CONTACTS, selective_set).apply();
                }
                else{
                    selective_set.remove(connections.get(position));
                    sharedPreferences.edit().putStringSet(SELECTIVE_CONTACTS, selective_set).apply();
                }

            }
        });


        holder.useridT.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ClipData clip = ClipData.newPlainText(null, holder.useridT.getText().toString().trim());
                if(clip != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mContext, "Copied to your clipboard", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


    }

    private void fillUserDetails(String uuid, final ImageView profileI, final TextView useridT, final TextView usernameT) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(uuid);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    User user = snapshot.getValue(User.class);
                    if(user != null) {
                        Log.i(TAG, "onDataChange: FOUND USER : "+user.getUsername());
//                        shimmer.stopShimmer();
//                        shimmer.setVisibility(View.GONE);
                        CustomBottomSheet.progressBar.setVisibility(View.GONE);
                        profileI.setImageResource(user.getProfile_photo());
                        useridT.setText(user.getShort_id());
                        usernameT.setText(user.getUsername());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout constrain;
        private ImageView profileI;
        private CheckBox checkBox;
        private TextView usernameT, useridT;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constrain = itemView.findViewById(R.id.bottom_sheet_constrain);
            profileI = itemView.findViewById(R.id.bottom_sheet_containerimage);
            checkBox = itemView.findViewById(R.id.bottom_sheet_checkBox);
            usernameT = itemView.findViewById(R.id.bottom_sheet_container_username);
            useridT = itemView.findViewById(R.id.bottom_sheet_container_uuid);
        }
    }

}
