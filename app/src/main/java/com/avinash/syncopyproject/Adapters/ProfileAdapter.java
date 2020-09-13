package com.avinash.syncopyproject.Adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

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

import anil.sardiwal.reboundrecycler.ReboundRecycler;

import static com.avinash.syncopyproject.Fragments.ProfileFragment.shimmer;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private static final String TAG = "ProfileAdapter";
    private Context mContext;
    private ArrayList<String> contact_list;
    private ConstraintLayout view;
    private ClipboardManager clipboard;
    private Dialog dialog;
    Boolean isCancellable = true;
    private FirebaseAuth mAuth;
    private Set<String> contact_set = new HashSet<>();

    public ProfileAdapter(Context context, ArrayList<String> contact_list) {
        mContext = context;
        this.contact_list = contact_list;

        try {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        catch (NullPointerException e){
            //Do something
        }
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.i(TAG, "Profile adapter starts");

        view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_container, parent, false);
        ReboundRecycler.first(view);
        return new ProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.i(TAG, "onBindViewHolder: RETREVING CONTACT DETAILS");
        ReboundRecycler.bind(holder.itemView, position);

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

        holder.deleteI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(contact_list.get(position));
            }
        });

        fillUserDetails(contact_list.get(position), holder.profileI, holder.useridT, holder.usernameT);

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
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
//                        shimmer.stopShimmer();
//                        shimmer.setVisibility(View.GONE);
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
        return contact_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView profileI, deleteI;
        private TextView usernameT, useridT;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileI = itemView.findViewById(R.id.profile_containerimage);
            deleteI = itemView.findViewById(R.id.profile_container_delete);
            usernameT = itemView.findViewById(R.id.profile_container_username);
            useridT = itemView.findViewById(R.id.profile_container_uuid);
//            shimmer = itemView.findViewById(R.id.profile_container_shimmer);
//            shimmer.setVisibility(View.VISIBLE);
//            shimmer.startShimmer();
        }
    }

    public void showAlertDialog(final String contactID)
    {


        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.delete_alert,
                        null);
        builder.setView(customLayout);

        customLayout.findViewById(R.id.delete_alert_noI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCancellable)
                    dialog.dismiss();
            }
        });

        customLayout.findViewById(R.id.delete_alert_yesI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancellable = false;
                customLayout.findViewById(R.id.showRemovingL).setVisibility(View.VISIBLE);
                removeContact(contactID);
            }
        });

        // create and show
        // the alert dialog
        dialog = builder
                .setCancelable(false)
                .create();
        dialog.show();
    }


    private void removeContact(final String contactID) {



        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference removeRef = null;
                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        String contact = s.getValue(String.class);
                        if(contact != null) {

                            if(contact.equals(contactID)){
                                removeRef = s.getRef();
                                updateSharedPref(contactID);
                            }

                        }

                    }

                    if(removeRef != null){
                        removeRef.removeValue();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("contact").child(contactID);
        mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                DatabaseReference removeRef = null;
                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        String contact = s.getValue(String.class);
                        if(contact != null) {

                            if(contact.equals(mAuth.getCurrentUser().getUid())){
                                removeRef = s.getRef();
                            }

                        }

                    }

                    if(removeRef != null){
                        removeRef.removeValue();
                        dialog.dismiss();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateSharedPref(String deleteID) {

//        SharedPreferences sharedPreferences = mContext.getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE);
//        contact_set = sharedPreferences.getStringSet(AutoService.CONNECTION_LIST, null);
//
//        for(String s : contact_set){
//
//            if(s.equals(deleteID)){
//                contact_set.remove(s);
//            }
//
//        }
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putStringSet(AutoService.CONNECTION_LIST, contact_set).apply();
    }


}
