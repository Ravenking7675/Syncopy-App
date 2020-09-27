package com.avinash.syncopyproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.Model.PcUser;
import com.avinash.syncopyproject.Model.User;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SearchIDActivity extends AppCompatActivity {

    public static final String PREF_PC_USERS = "pc_users";

    private static final String TAG = "SearchIDActivity";
    private ImageView cancelI;
    private ImageView showQRI;
    private Button scanB;
    private Button searchB;
    private TextView searchT;

    private ImageView defaultI;
    private ImageView failI;
    private ImageView overflowI;
    private ShimmerFrameLayout shimmer;

    private ImageView searchBackgroundI;
    private ImageView searchProfileI;
    private TextView searchUsernameT;
    private TextView searchUUIDT;
    private TextView alreadyConnectedT;
    private Button continueB;
    private FirebaseAuth mAuth;

    private String connectionId;
    private String connectionUsername;
    private int connectionImage;
    private String connectionShortId;

    private ArrayList<String> pc_connections;
    private SharedPreferences sharedPreferences;
    private Vibrator vibre;
    private String pc_uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_i_d);

        scanB = findViewById(R.id.scanButton);
        cancelI = findViewById(R.id.cancel_searchI);
        showQRI = findViewById(R.id.showQRI_search);
        searchB = findViewById(R.id.searchB_search);
        searchT = findViewById(R.id.searchClipT);
        defaultI = findViewById(R.id.search_defaultI);
        failI = findViewById(R.id.search_failI);
        shimmer = findViewById(R.id.search_Shimmer);
        searchBackgroundI = findViewById(R.id.search_result_background);
        searchProfileI = findViewById(R.id.search_image_I);
        searchUsernameT = findViewById(R.id.search_result_usernameT);
        searchUUIDT = findViewById(R.id.search_result_idT);
        alreadyConnectedT = findViewById(R.id.alreadyConnectedT);
        continueB = findViewById(R.id.continueSearchB);
        overflowI = findViewById(R.id.search_overflowI);

        final ConstraintLayout con = findViewById(R.id.searchUserLayout);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(con);
            }
        });

        pc_connections = new ArrayList<>();

        sharedPreferences = getSharedPreferences(HomeFragment.SHARED_PREF, MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        scanB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });

        vibre = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);


        cancelI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                intent.putExtra(SyncopyActivity.FRAGMENT_NO, 1);
                startActivity(intent);
                finish();
            }
        });

        showQRI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyQRActivity.class);
                startActivity(intent);
            }
        });

        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(searchB);
                scanB.setVisibility(View.VISIBLE);
                defaultI.setVisibility(View.VISIBLE);
                failI.setVisibility(View.GONE);
                shimmer.setVisibility(View.GONE);
                searchBackgroundI.setVisibility(View.GONE);
                searchProfileI.setVisibility(View.GONE);
                searchUsernameT.setVisibility(View.GONE);
                searchUUIDT.setVisibility(View.GONE);
                continueB.setVisibility(View.GONE);
                alreadyConnectedT.setVisibility(View.GONE);
                overflowI.setVisibility(View.GONE);

                searchInFirebase();
            }
        });

        continueB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(continueB.getTag().equals("android"))
                    addToFirebase();
                if(continueB.getTag().equals("pc"))
                    connectWithPC();

            }
        });

    }


    private void searchInFirebase() {

        final String text = searchT.getText().toString().trim();
        if(text.isEmpty()){
            searchT.requestFocus();
            searchT.setError("This field can not be empty", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot));
        }
        else if(text.length() < 6){
            searchT.requestFocus();
            searchT.setError("Please check the Clip ID again", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot));
        }
        else if(text.substring(0, 3).equals("pc-") && text.length() == 9) {
            Log.i(TAG, "searchInFirebase: THIS IS PC ID");
            pc_uuid = text;
            searchPCUser(pc_uuid);
        }
        else{

            defaultI.setVisibility(View.GONE);
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmer();

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user");
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean found = false;
                    if(snapshot.exists()){
                        for(DataSnapshot s : snapshot.getChildren()){
                            User user = s.getValue(User.class);
                            if(user != null){
                                if(user.getShort_id().equals(text) && !user.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                    found = true;
                                    connectionId = user.getUser_id();
                                    connectionImage = user.getProfile_photo();
                                    connectionUsername = user.getUsername();
                                    connectionShortId = text;
                                }
                            }
                        }

                        if(found){
                            checkAlreadyConnected();
                        }
                        else{
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                            failI.setVisibility(View.VISIBLE);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void hideKeyboard(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void connectWithPC() {
        try {
            pc_connections = new ArrayList<>(sharedPreferences.getStringSet(PREF_PC_USERS, null));
            Log.i(TAG, "connectWithPC: INITIAL CONNECTION SIZE : " + pc_connections.size());
            for(String i : pc_connections){
                Log.i(TAG, "connectWithPC: CONNECTIONS : "+i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user_web").child(pc_uuid);
        Map<String, Object> map = new HashMap<>();
        map.put("connectedTo", mAuth.getCurrentUser().getUid());
        mRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "onComplete: ADDED PC USER SUCCESSFULLY");

                    try{
                        Log.i(TAG, "UUID : "+pc_uuid);
                        pc_connections.add(pc_uuid);
                        sharedPreferences.edit().putStringSet(PREF_PC_USERS, new HashSet<String>(pc_connections)).apply();

                    }catch (Exception e){
                        //Do something
                    }
                    Log.i(TAG, "onComplete: PC CONNECTION SIZE : " + pc_connections.size());
                    vibre.vibrate(VibrationEffect.createOneShot(20, 20));
                    Intent intent = new Intent(SearchIDActivity.this, SyncopyActivity.class);
                    intent.putExtra(SyncopyActivity.FRAGMENT_NO, 2);
                    startActivity(intent);
                    finish();
                }
                else{
                    Log.i(TAG, "onComplete: FAILED TO ADD PC USER");
                }
            }
        });

    }


    private void searchPCUser(String uuid) {

        defaultI.setVisibility(View.GONE);
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user_web").child(uuid);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Found user
                    PcUser pcUser = snapshot.getValue(PcUser.class);
                    if(pcUser != null){
                        Log.i(TAG, "FOUND PC USER : "+pcUser.getPcName());
//                        if(!pcUser.getConnectedTo().equals(mAuth.getCurrentUser().getUid()) && pcUser.getConnectedTo().equals("-1")) {
                        if(pcUser.getConnectedTo().equals("unknown")) {

                            defaultI.setVisibility(View.GONE);

                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);

                            searchBackgroundI.setVisibility(View.VISIBLE);
                            searchProfileI.setVisibility(View.VISIBLE);
                            searchUsernameT.setVisibility(View.VISIBLE);
                            searchUUIDT.setVisibility(View.VISIBLE);

                            searchBackgroundI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_placeholder_ok));

                            if(pcUser.getPcName().equals("windows"))
                                searchProfileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_windows_logo));
                            else
                                searchProfileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_linux_logo));

                            searchUsernameT.setText(pcUser.getPcName());
                            searchUUIDT.setText("PC ID : " + pcUser.getUuid());

                            scanB.setVisibility(View.INVISIBLE);
                            continueB.setVisibility(View.VISIBLE);
                            continueB.setTag("pc");
                        }
                        else{

                            defaultI.setVisibility(View.GONE);

                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);

                            searchBackgroundI.setVisibility(View.VISIBLE);
                            searchProfileI.setVisibility(View.VISIBLE);
                            searchUsernameT.setVisibility(View.VISIBLE);
                            searchUUIDT.setVisibility(View.VISIBLE);

                            if(pcUser.getPcName().equals("windows"))
                                searchProfileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_windows_logo));
                            else
                                searchProfileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_linux_logo));

                            searchUsernameT.setText(pcUser.getPcName());
                            searchUUIDT.setText("PC ID : " + pcUser.getUuid());

                            alreadyConnectedT.setVisibility(View.VISIBLE);
                            alreadyConnectedT.setText("This user is not available.");

                        }

                    }
                }
                else{
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    failI.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkAlreadyConnected() {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean exsists = false;
                if(snapshot.exists()) {

                    if(snapshot.getChildrenCount() < 6 ) {

                    for (DataSnapshot s : snapshot.getChildren()) {

                        String connection = s.getValue(String.class);
                        if (connection != null) {
                            if (connection.equals(connectionId)) {
                                exsists = true;
                            }
                        }

                    }

                    if (exsists) {
                        defaultI.setVisibility(View.GONE);

                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);

                        searchBackgroundI.setVisibility(View.VISIBLE);
                        searchProfileI.setVisibility(View.VISIBLE);
                        searchUsernameT.setVisibility(View.VISIBLE);
                        searchUUIDT.setVisibility(View.VISIBLE);

                        searchBackgroundI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_placeholder_no));
                        searchProfileI.setImageResource(connectionImage);
                        searchUsernameT.setText(connectionUsername);
                        searchUUIDT.setText("Clip ID : " + connectionShortId);
                        alreadyConnectedT.setVisibility(View.VISIBLE);
                        alreadyConnectedT.setText("You're already connected with this user.");
                    } else {
                        defaultI.setVisibility(View.GONE);

                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);

                        searchBackgroundI.setVisibility(View.VISIBLE);
                        searchProfileI.setVisibility(View.VISIBLE);
                        searchUsernameT.setVisibility(View.VISIBLE);
                        searchUUIDT.setVisibility(View.VISIBLE);


                        searchBackgroundI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_placeholder_ok));
                        searchProfileI.setImageResource(connectionImage);
                        searchUsernameT.setText(connectionUsername);
                        searchUUIDT.setText("Clip ID : " + connectionShortId);

                        scanB.setVisibility(View.INVISIBLE);
                        continueB.setVisibility(View.VISIBLE);
                        continueB.setTag("android");

                    }
                }

                    else{
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        overflowI.setVisibility(View.VISIBLE);
                    }

                }
                else{
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);

                    searchBackgroundI.setVisibility(View.VISIBLE);
                    searchProfileI.setVisibility(View.VISIBLE);
                    searchUsernameT.setVisibility(View.VISIBLE);
                    searchUUIDT.setVisibility(View.VISIBLE);


                    searchBackgroundI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_placeholder_ok));
                    searchProfileI.setImageResource(connectionImage);
                    searchUsernameT.setText(connectionUsername);
                    searchUUIDT.setText("Clip ID : "+connectionShortId);

                    scanB.setVisibility(View.INVISIBLE);
                    continueB.setVisibility(View.VISIBLE);
                    continueB.setTag("android");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addToFirebase() {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());
        mRef.push().setValue(connectionId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_SHORT).show();
                    vibre.vibrate(VibrationEffect.createOneShot(20, 20));
                    Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                    intent.putExtra(SyncopyActivity.FRAGMENT_NO, 4);
                    startActivity(intent);
                    finish();
                }
            }
        });

        DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("contact").child(connectionId);
        mRef2.push().setValue(mAuth.getCurrentUser().getUid());

    }

    private void startScanActivity() {


        Intent intent = new Intent(SearchIDActivity.this, ScanActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(SearchIDActivity.this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);

    }
}