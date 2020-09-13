package com.avinash.syncopyproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.EditTextAdapter;
import com.avinash.syncopyproject.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {

    public static final String USERID = "userid";
    public static final String USERNAME = "username";
    private static final String TAG = "EditProfileActivity";
    private ArrayList<Integer> profile_images = new ArrayList<>();
    private RecyclerView recyclerView;
    private ArrayList<String> profile_names = new ArrayList<>();
    private TextView charNameT;
    private Button continueB;
    private TextView usernameT;
    private int profileImage = R.drawable.ic_fox;
    private String usernameF;
    private String userId;
    private Dialog dialog;
    private String shortend_userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        charNameT = findViewById(R.id.characterNameT);
        continueB = findViewById(R.id.continueAlertB);
        usernameT = findViewById(R.id.usernameT);

        profile_images.add(R.drawable.no_item);
        profile_images.add(R.drawable.ic_bird);
        profile_images.add(R.drawable.ic_bird_pink);
        profile_images.add(R.drawable.ic_boy);
        profile_images.add(R.drawable.ic_cat);
        profile_images.add(R.drawable.ic_fox);
        profile_images.add(R.drawable.ic_ghost);
        profile_images.add(R.drawable.ic_girl);
        profile_images.add(R.drawable.ic_monkey);
        profile_images.add(R.drawable.ic_panda);
        profile_images.add(R.drawable.ic_pig);
        profile_images.add(R.drawable.ic_skull);
        profile_images.add(R.drawable.no_item);

        profile_names.add("");
        profile_names.add("Tweety");
        profile_names.add("Fido");
        profile_names.add("Covid guy");
        profile_names.add("Tom");
        profile_names.add("Swiper");
        profile_names.add("Boo");
        profile_names.add("Covid girl");
        profile_names.add("Coco");
        profile_names.add("Baby Panda");
        profile_names.add("Pinky Pig");
        profile_names.add("Mr. Skull");
        profile_names.add("");

        usernameF = getIntent().getExtras().getString(USERNAME);
        userId = getIntent().getExtras().getString(USERID);

        if(!usernameF.isEmpty()) {
            usernameT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box_selected ));
        }
        usernameT.setText(usernameF);

        final Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        recyclerView = findViewById(R.id.recycler_view_ediit_profile);
        recyclerView.setHasFixedSize(true);

        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        CenterZoomLayoutManager c = new CenterZoomLayoutManager(this, CenterZoomLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(c);
        EditTextAdapter adapter = new EditTextAdapter(profile_images);
        recyclerView.setAdapter(adapter);

        setCurrentItem(6, true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    v.vibrate(VibrationEffect.createOneShot(20, 20));
                    int position = getCurrentItem();
                    if(position != -1){
                        charNameT.setText(profile_names.get(position));
                        profileImage = profile_images.get(position);
                    }
//                    Log.i(TAG, "onScrollStateChanged: ");
                    Log.i(TAG, "This is page "+position);
                }
                if(newState == RecyclerView.SCREEN_STATE_ON){
//                    Log.i(TAG, "onScrollStateChanged: Scrolling");
                }
            }
        });

        LinearSnapHelper linearSnapHelper = new SnapHelperOneByOne();
        linearSnapHelper.attachToRecyclerView(recyclerView);
        Log.i(TAG, "onCreate: STARTING RECYCLER VIEW");

        continueB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveUserInfo();

            }
        });

        usernameT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()){
                    usernameT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box ));
                }
                else{
                    usernameT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box_selected ));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void saveUserInfo() {

        String username = usernameT.getText().toString().trim();
        if(username.isEmpty()){
            usernameT.requestFocus();
            usernameT.setError("This field can not be empty", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot));
        }
        else if(username.length() > 16){
            usernameT.requestFocus();
            usernameT.setError("Username can not be more than 16 characters", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dot));
        }
        else {
            showAlertDialog();

            shortend_userID = generateUserID(userId);

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user");
            mRef.child(userId).setValue(new User(userId, shortend_userID, username, profileImage, false))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                updateUI();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Something went wrong, try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private String generateUserID(String userID) {

        return userID.substring(6,12);

    }

    private int getCurrentItem() {

        return ((CenterZoomLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

    }

    private void setCurrentItem(int position, boolean smooth){

        if(smooth)
            recyclerView.smoothScrollToPosition(position);
        else
            recyclerView.scrollToPosition(position);

    }

    public static class SnapHelperOneByOne extends LinearSnapHelper {

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {

            if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
                return RecyclerView.NO_POSITION;
            }

            final View currentView = findSnapView(layoutManager);

            if (currentView == null) {
                return RecyclerView.NO_POSITION;
            }

            LinearLayoutManager myLayoutManager = (LinearLayoutManager) layoutManager;

            int position1 = myLayoutManager.findFirstVisibleItemPosition();
            int position2 = myLayoutManager.findLastVisibleItemPosition();

            int currentPosition = layoutManager.getPosition(currentView);

            if (velocityX > 400) {
                currentPosition = position2;
            } else if (velocityX < 400) {
                currentPosition = position1;
            }

            if (currentPosition == RecyclerView.NO_POSITION) {
                return RecyclerView.NO_POSITION;
            }

            return currentPosition;
        }
    }

    public void showAlertDialog()
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);

        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.alter_loading,
                        null);
        builder.setView(customLayout);

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void updateUI() {
        Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
        intent.putExtra(SyncopyActivity.USERID, userId);

        startActivity(intent);
        finishAffinity();
    }

}