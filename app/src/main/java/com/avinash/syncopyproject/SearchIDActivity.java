package com.avinash.syncopyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

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

public class SearchIDActivity extends AppCompatActivity {

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

        mAuth = FirebaseAuth.getInstance();

        scanB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanActivity();
            }
        });

        cancelI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                startActivity(intent);
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

                addToFirebase();

            }
        });

    }

    private void searchInFirebase() {

        final String text = searchT.getText().toString().trim();
        if(text.isEmpty()){
            searchT.requestFocus();
            searchT.setError("This field can not be empty", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot));
        }
        else if(text.length() != 6){
            searchT.requestFocus();
            searchT.setError("Please check the Clip ID again", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot));
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
                    Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                    startActivity(intent);
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