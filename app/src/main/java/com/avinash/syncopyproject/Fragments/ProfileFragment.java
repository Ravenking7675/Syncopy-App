package com.avinash.syncopyproject.Fragments;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.ProfileAdapter;
import com.avinash.syncopyproject.BottomSheetProfile;
import com.avinash.syncopyproject.MyQRActivity;
import com.avinash.syncopyproject.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import anil.sardiwal.reboundrecycler.ReboundRecycler;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static android.view.View.GONE;


public class ProfileFragment extends Fragment {

    public static final String PREF_CONNECTION_COUNT = "connection count";

    private static final String TAG = "ProfileFragment";
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<String> contact_list;
    private ImageView defaultI;
    private FirebaseAuth mAuth;
    private ProfileAdapter adapter;
    public static ShimmerFrameLayout shimmer;
    private SharedPreferences sharedPreferences;

    private ProgressBar progressBar;
    private ImageView profileI;
    private TextView profile_usernameT;
    private TextView profile_uuidT;
    private Button logoutB;
    private TextView profile_connection_count;
    private ClipboardManager clipboard;
    private Dialog dialog;

    private ImageView logoutI;

    private Boolean isCancellable = true;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view =  inflater.inflate(R.layout.fragment_profile, container, false);

        clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        recyclerView  = view.findViewById(R.id.recycler_view_profile);
        defaultI = view.findViewById(R.id.profile_emptyI);
        shimmer = view.findViewById(R.id.profile_Shimmer);
        progressBar = view.findViewById(R.id.profile_progressBar);
        profileI = view.findViewById(R.id.profilePhotoI);
        profile_usernameT = view.findViewById(R.id.profile_usernameT);
        profile_uuidT = view.findViewById(R.id.profile_useridT);
        logoutB = view.findViewById(R.id.profile_logoutB);
        profile_connection_count = view.findViewById(R.id.profile_connection_countT);
        progressBar.setMax(6);
        logoutI = view.findViewById(R.id.logout_gradientI);
        mAuth = FirebaseAuth.getInstance();

        searchConnections();
        updateUserInfo();

        final Vibrator vib = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);


        recyclerView.setHasFixedSize(true);

        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager l = new LinearLayoutManager(getContext());
        l.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(l);

        //Libraries
        ReboundRecycler.init(recyclerView);
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        logoutI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    vib.vibrate(VibrationEffect.createOneShot(15, 10));
                    BottomSheetProfile bottomSheet = new BottomSheetProfile();
                    bottomSheet.isCancelable();
                    bottomSheet.show(getFragmentManager(), "exampleBottomSheet");
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });

//        logoutB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                showAlertDialog();
//
//            }
//        });

        profileI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), MyQRActivity.class);
                startActivity(intent);

            }
        });

        profile_uuidT.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ClipData clip = ClipData.newPlainText(null, profile_uuidT.getText().toString().trim());
                if(clip != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "Copied to your clipboard", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        return view;
    }

//    public void showAlertDialog()
//    {
//
//
//        // Create an alert builder
//        AlertDialog.Builder builder
//                = new AlertDialog.Builder(getContext());
//
//        // set the custom layout
//        final View customLayout
//                = getLayoutInflater()
//                .inflate(
//                        R.layout.logout_alert,
//                        null);
//        builder.setView(customLayout);
//
//        customLayout.findViewById(R.id.logout_alert_noI).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isCancellable)
//                    dialog.dismiss();
//            }
//        });
//
//        customLayout.findViewById(R.id.logout_alert_yesI).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isCancellable = false;
//                customLayout.findViewById(R.id.logout_showRemovingL).setVisibility(View.VISIBLE);
//                logoutUser();
//            }
//        });
//
//        // create and show
//        // the alert dialog
//        dialog = builder
//                .setCancelable(false)
//                .create();
//        dialog.show();
//    }
//
//    private void logoutUser() {
//
//
//        try {
//            getContext().getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE).edit().clear().apply();
//
//            Log.i(TAG, "logoutUser: LOGGING OUT");
//
//            //firebase logout
//            FirebaseAuth.getInstance().signOut();
//            Log.i(TAG, "logoutUser: FIREBASE LOGOUT DONE");
//            //facebook logout
//            try {
//                if(LoginManager.getInstance() != null) {
//                    LoginManager.getInstance().logOut();
//                    Log.i(TAG, "logoutUser: FACEBOOK LOGOUT DONE");
//                }
//            }catch (Exception e){
//                //Do something
//                e.printStackTrace();
//            }
//
//            //google logout
//            try {
//                GoogleSignInOptions gso = new GoogleSignInOptions
//                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
//                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
//                googleSignInClient.signOut();
//                Log.i(TAG, "logoutUser: GOOGLE LOGOUT DONE");
//            }
//            catch (Exception e){
//
//                //Do something
//                e.printStackTrace();
//            }
//
//            Intent intent = new Intent(getContext(), LoginActivity.class);
//            startActivity(intent);
//
//        }catch (NullPointerException e){
//            //Do something
//            e.printStackTrace();
//        }
//
//    }


    private void updateUserInfo() {

        try {
            sharedPreferences = getContext().getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE);
            String username = sharedPreferences.getString(HomeFragment.USERNAME, "");
            int profile_image = sharedPreferences.getInt(HomeFragment.PHOTO, R.drawable.ic_default_skull);
            String userId = sharedPreferences.getString(HomeFragment.SHORT_UUID, "");
            int connection_count = sharedPreferences.getInt(PREF_CONNECTION_COUNT, 0);

            progressBar.setProgress(connection_count);
            profileI.setImageResource(profile_image);
            profile_usernameT.setText(username);
            profile_uuidT.setText(userId);

            if(connection_count == 6){
                profile_connection_count.setTextColor(getResources().getColor(R.color.error_box));
            }
            else{
                profile_connection_count.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            profile_connection_count.setText(connection_count+"/6");

        }
        catch (NullPointerException e){
            //Do something
        }


    }


    private void addToRecyclerView() {

        Log.i(TAG, "addToRecyclerView: START");
        adapter = new ProfileAdapter(getContext(), contact_list);
        recyclerView.setAdapter(adapter);

    }

    private void searchConnections() {

        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contact_list = new ArrayList<>();

                if(snapshot.exists()){

                    for(DataSnapshot s : snapshot.getChildren()){

                        String contact_id = s.getValue(String.class);
                        if(contact_id != null){

                            contact_list.add(contact_id);
                        }

                    }

                    if(contact_list.size() > 0){
                        Log.i(TAG, "onDataChange: CONTACT FOUND : "+contact_list.size());
                        defaultI.setVisibility(GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        try{

                            sharedPreferences.edit().putInt(PREF_CONNECTION_COUNT, contact_list.size()).apply();
                            updateUserInfo();
                        }catch (NullPointerException e){
                            //Do something
                            e.printStackTrace();
                        }

                        addToRecyclerView();
                    }
                    else{
                        Log.i(TAG, "onDataChange: FAILED");
                        defaultI.setVisibility(View.VISIBLE);
                        shimmer.stopShimmer();
                        shimmer.setVisibility(GONE);
                        recyclerView.setVisibility(GONE);

                    }

                }
                else{

                    try{

                        sharedPreferences.edit().putInt(PREF_CONNECTION_COUNT, contact_list.size()).apply();
                        updateUserInfo();
                    }catch (NullPointerException e){
                        //Do something
                        e.printStackTrace();
                    }

                    Log.i(TAG, "onDataChange: FAILED");
                    defaultI.setVisibility(View.VISIBLE);
                    shimmer.stopShimmer();
                    shimmer.setVisibility(GONE);
                    recyclerView.setVisibility(GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}