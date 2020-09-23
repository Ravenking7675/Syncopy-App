package com.avinash.syncopyproject.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.CustomBottomSheet;
import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.MyBounceInterpolator;
import com.avinash.syncopyproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.avinash.syncopyproject.Fragments.HomeFragment.SHARED_PREF;
import static com.avinash.syncopyproject.Services.AutoService.CONNECTION_LIST;
import static com.avinash.syncopyproject.Services.AutoService.PREV_CLIP;
import static com.avinash.syncopyproject.Services.AutoService.sharedClip;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private int progress =0;
    private static final String TAG = "HomeAdapter";
    private ArrayList<Integer> modes;
    private Context mContext;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    private Set<String> defaultSet = new HashSet<>();

    public HomeAdapter(ArrayList<Integer> modes, Context mContext, ProgressBar progressBar) {
        mAuth = FirebaseAuth.getInstance();
        Log.i(TAG, "HomeAdapter: GOT THE VALUES");
        this.modes = modes;
        this.mContext = mContext;
        this.mProgressBar = progressBar;
        defaultSet.add(mAuth.getCurrentUser().getUid());
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.i(TAG, "ViewHolder: INFLATING THE IMAGE");
            image = itemView.findViewById(R.id.image_modes_container);
        }
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: START");
        ImageView view = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.modes_container, parent, false);


        return new HomeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "onBindViewHolder: START");
        holder.image.setImageResource(modes.get(position));

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(HomeFragment.SHARED_PREF, MODE_PRIVATE);
        if(!sharedPreferences.getBoolean(HomeFragment.AUTO_MANUAL, false)) {

            final Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce_animation);

            // Use bounce interpolator with amplitude 0.2 and frequency 20
            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 10);
            myAnim.setInterpolator(interpolator);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.image.startAnimation(myAnim);
                    listenToClipChange(position);
                }
            });
        }

        if(position == 3){
            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CustomBottomSheet bottomSheet = new CustomBottomSheet();
                    bottomSheet.isCancelable();
                    bottomSheet.show(((FragmentActivity)mContext).getSupportFragmentManager(), "exampleBottomSheet");
                    return true;
                }
            });

        }


    }

    @Override
    public int getItemCount() {
        return modes.size();
    }

    private void listenToClipChange(int mode){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String newClip = "";

        String prevClip = sharedPreferences.getString(PREV_CLIP, "");

            try {
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData c = clipboard.getPrimaryClip();

                if(c != null)
                    newClip = c.getItemAt(0).getText().toString().trim();

                if (!sharedPreferences.getString(PREV_CLIP, "").equals(newClip) ) {
                    Log.i(TAG, "Clip is not same");
                    Log.i(TAG, "Shared Clip : "+sharedClip.getItemAt(0).getText().toString().trim());
//                    if(!sharedClip.getItemAt(0).getText().toString().trim().equals(newClip)) {
                        Log.i(TAG, "You created this clip buddy");
                        sharedPreferences.edit().putString(PREV_CLIP, newClip).apply();
                        Log.i(TAG, "listenToClipChange: SENDING >>> : " + newClip);
                        sendToFirebase(mode, newClip);
//                    }
//                    new RunInBackground().execute("avinash");
                }
                else{
                    Log.i(TAG, "listenToClipChange: CLIP IS SAME");
                }


            } catch (Exception e) {
//                e.printStackTrace();
            }
    }


    private void sendToFirebase(int mode, String clip) {


        Calendar rightNow = Calendar.getInstance();

        long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                rightNow.get(Calendar.DST_OFFSET);
        long milliSeconds = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000);

        HashMap<String, Object> map = new HashMap<>();
        map.put("time", milliSeconds);
        map.put("clip", clip);

        if(mode == 1)
            map.put("history", false);
        else
            map.put("history", true);

//        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("clip").child(mAuth.getCurrentUser().getUid());
//
//        mRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                    Log.i(TAG, "onComplete: SUCCESSFULLY SENT");
//                    Toast.makeText(mContext, "clip send successfully", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Log.i(TAG, "FAILED TO SEND CLIP : "+task.getException());
//                }
//            }
//        });

        try {

            if (mode == 1 || mode == 2) {

                Log.i(TAG, "SHARED PREF DATA MANUAL : "+mContext.getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(CONNECTION_LIST, defaultSet).size());

                for (final String userID : mContext.getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(CONNECTION_LIST, defaultSet)) {

                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("clip").child(userID);

                    mRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: SUCCESSFULLY SENT");
                                if(userID.equals(mAuth.getCurrentUser().getUid()))
                                    Toast.makeText(mContext, "clip send successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i(TAG, "FAILED TO SEND CLIP : " + task.getException());
                            }
                        }
                    });
                }

            }

            else{

                Log.i(TAG, "SHARED PREF DATA MANUAL : "+mContext.getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(BottomSheetAdapter.SELECTIVE_CONTACTS, defaultSet).size());

                for (final String userID : mContext.getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(BottomSheetAdapter.SELECTIVE_CONTACTS, defaultSet)) {

                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("clip").child(userID);

                    mRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: SUCCESSFULLY SENT");
                                if(userID.equals(mAuth.getCurrentUser().getUid()))
                                    Toast.makeText(mContext, "clip send successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i(TAG, "FAILED TO SEND CLIP : " + task.getException());
                            }
                        }
                    });
                }

            }
        }
        catch (NullPointerException e ){
            e.printStackTrace();
        }
    }

}
