package com.avinash.syncopyproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.avinash.syncopyproject.Adapters.ConnectionAdapter;
import com.avinash.syncopyproject.Fragments.CommandFragment;
import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.Fragments.LinkFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jackandphantom.blurimage.BlurImage;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static android.view.View.GONE;
import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class ScreenshotActivity extends AppCompatActivity {

    private static final String TAG = "ScreenshotActivity";
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private WormDotsIndicator wormDotsIndicator;
    private ImageView profileI;
    private TextView nameT;
    private ImageView backI;
    private SubsamplingScaleImageView screenshotI;
    private Button screenshotB;
    private Bitmap bitmap;
    private FirebaseAuth mAuth;
    private ImageView screenshot_backI;
    private DatabaseReference thumbnail;
    private DatabaseReference ss;
    private ProgressBar progressBar;
    private ImageView loadingI;
    private ImageView defaultI;
    private Vibrator v;
    private Boolean isRunning = false;
    private ImageView saveI;
    private OutputStream outputStream;
    public static String pc_uuid;
    private int pc_type;
    private ImageView windowsDefaultI;
    private SharedPreferences sharedPreferences;
    private Set<String> pc_users;
    private Dialog dialog;

    @Override
    protected void onRestart() {
        super.onRestart();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot);

        viewPager = findViewById(R.id.view_pager_screenshot);
        wormDotsIndicator = findViewById(R.id.worm_dots_indicator_screenshot);
        profileI = findViewById(R.id.profile_screenshotI);
        nameT = findViewById(R.id.name_screenshotT);
        backI = findViewById(R.id.back_screenshotI);
        screenshotI = findViewById(R.id.screenshotI);
        screenshotB = findViewById(R.id.screenshotB);
        screenshot_backI = findViewById(R.id.screenshot_rawI);
        progressBar = findViewById(R.id.progressBar_screenshot_image);
        loadingI = findViewById(R.id.screenshot_loadingI);
        defaultI = findViewById(R.id.screenshot_defaultI);
        saveI = findViewById(R.id.saveImageI);
        windowsDefaultI = findViewById(R.id.windowsDefaultI);

        sharedPreferences = getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE);

        try{

            String name = getIntent().getExtras().getString(ConnectionAdapter.INTENT_PC_NAME);
            pc_type = getIntent().getExtras().getInt(ConnectionAdapter.INTENT_PC_TYPE);
            pc_uuid = getIntent().getExtras().getString(ConnectionAdapter.INTENT_PC_UUID);
            nameT.setText(name);

            if(pc_type == 1){
                profileI.setImageResource(R.drawable.ic_linux_logo);
            }
            else
                profileI.setImageResource(R.drawable.ic_windows_logo);

        }catch (Exception e){
            e.printStackTrace();
        }

        defaultI.setVisibility(View.VISIBLE);
        screenshot_backI.setVisibility(View.INVISIBLE);
        screenshotI.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        saveI.setVisibility(GONE);
        mAuth = FirebaseAuth.getInstance();

        //PC TYPE HERE


        if(pc_type != 1){
            windowsDefaultI.setVisibility(View.VISIBLE);
            wormDotsIndicator.setVisibility(GONE);
            viewPager.setVisibility(GONE);
        }
        else {
            windowsDefaultI.setVisibility(GONE);
            wormDotsIndicator.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            fragments.add(new CommandFragment());
            fragments.add(new LinkFragment());

            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            ViewPagerAdapterScreenshot adapter = new ViewPagerAdapterScreenshot(getSupportFragmentManager(), fragments);
            viewPager.setAdapter(adapter);
            OverScrollDecoratorHelper.setUpOverScroll(viewPager);
            wormDotsIndicator.setViewPager(viewPager);
        }
        getLink();

        backI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (!isFinishing ()) {
//                    Glide.with(getApplicationContext()).pauseRequests();
//                }
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_status").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("status");
                mRef.setValue("-1").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: REMOVED STATUS TO -1");
                        }
                    }
                });

                if(ss != null && thumbnail != null) {
                    Log.i(TAG, "going back: STARTS REMOVING VALUE");
                    ss.setValue("-1");
                    thumbnail.setValue("-1").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.i(TAG, "going back: REMOVED THE THUMBNAIL");
                            }
                            else{
                                Log.i(TAG, "going back: FAILED REMOVING THUMBNAIL");
                            }
                        }
                    });
                    saveI.setVisibility(GONE);
                }

                Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                intent.putExtra(SyncopyActivity.FRAGMENT_NO, 2);
                startActivity(intent);
                finish();

            }
        });

        screenshotB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRunning) {
                    screenshot_backI.setVisibility(View.INVISIBLE);
                    screenshotI.setVisibility(View.INVISIBLE);
                    loadingI.setVisibility(View.VISIBLE);
                    defaultI.setVisibility(GONE);
                    saveI.setVisibility(GONE);
                    isRunning = true;
                    Log.i(TAG, "onClick: STARTING SCREENSHOT...");
                    setRequest();
                }
                else{
                    Log.i(TAG, "onClick: FAILED");
//                    Toast.makeText(getApplicationContext(), "Another process is already running", Toast.LENGTH_SHORT).show();
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.constran_layout_snackbar), "Another process is already running", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                    snackbar.show();
                }
            }
        });

        pcUserQuitListener();

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
                        R.layout.disconnected_alert,
                        null);
        builder.setView(customLayout);

        Button revertB = customLayout.findViewById(R.id.disconnected_revertB);
        final LinearLayout showProgress = customLayout.findViewById(R.id.showRemovingL_alert_disconnected);
        revertB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgress.setVisibility(View.VISIBLE);
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_status").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("status");
                mRef.setValue("-1").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "onComplete: REMOVED STATUS TO -1");
                        }
                    }
                });

                if (ss != null && thumbnail != null) {
                    Log.i(TAG, "going back: STARTS REMOVING VALUE");
                    ss.setValue("-1");
                    thumbnail.setValue("-1").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "going back: REMOVED THE THUMBNAIL");
                            } else {
                                Log.i(TAG, "going back: FAILED REMOVING THUMBNAIL");
                            }
                        }
                    });
                    saveI.setVisibility(GONE);
                    Intent intent = new Intent(ScreenshotActivity.this, SyncopyActivity.class);
                    intent.putExtra(SyncopyActivity.FRAGMENT_NO, 2);
                    startActivity(intent);
                    finish();
                }
            }
        });



        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void pcUserQuitListener() {

        try{
            progressBar.setVisibility(View.VISIBLE);
            pc_users = sharedPreferences.getStringSet(SearchIDActivity.PREF_PC_USERS, new HashSet<String>());

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("status");

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){

                        for(DataSnapshot s : snapshot.getChildren()) {

                            if(pc_uuid.equals(s.getKey())) {
                                Boolean status = s.getValue(Boolean.class);
                                if (!status) {
//                                    Intent intent = new Intent(ScreenshotActivity.this, SyncopyActivity.class);
//                                    //put-extra
//                                    startActivity(intent);
                                    try {
                                        showAlertDialog();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }

                        }





                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void setRequest() {
        Log.i(TAG, "setRequest: SETTING REQUEST");
        progressBar.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance();
        int mseconds = c.get(Calendar.MILLISECOND);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("request").child(mAuth.getCurrentUser().getUid()).child("status");
        mRef.setValue(mseconds);


    }

    private void getLink() {
        Log.i(TAG, "getLink: GETTING LINK");
        thumbnail = FirebaseDatabase.getInstance().getReference("thumbnail").child(mAuth.getCurrentUser().getUid()).child("image");
        thumbnail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    Log.i(TAG, "onDataChange: SCREENSHOT EXISTS");
                    String imageURL = snapshot.getValue(String.class);
                    if (imageURL != null) {
                        Log.i(TAG, "onDataChange: IMAGE IS NOT NULL");
                        if (!imageURL.equals("-1")) {
                            Log.i(TAG, "onDataChange: IMAGE EXISTS");
                            try {
                                Log.i(TAG, "onDataChange: SETTING UP GLIDE");
                                if(getApplicationContext() != null) {
                                    Glide.with(getApplicationContext())
                                            .asBitmap()
                                            .load(imageURL)
                                            .placeholder(R.drawable.ic_sit_relax)
                                            .transition(withCrossFade())
                                            .error(R.drawable.ic_error_screenshot)
                                            .into(new CustomTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    Log.i(TAG, "onResourceReady: THUBNAIL IS READY");
                                                    screenshot_backI.setVisibility(View.VISIBLE);
                                                    loadingI.setVisibility(View.INVISIBLE);
                                                    progressBar.setVisibility(View.VISIBLE);
                                                    BlurImage.with(ScreenshotActivity.this).load(resource).intensity(2).Async(true).into(screenshot_backI);
//                                            bitmap = BlurImage.with(ScreenshotActivity.this).load(resource).intensity(1).Async(true).getImageBlur();
                                                    Log.i(TAG, "onResourceReady: THUMBNAIL DONE");
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                                    Log.i(TAG, "onLoadCleared: PLACEHOLDER READY");
                                                }

                                            });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.i(TAG, "onDataChange: SOMETHING WENT WRONG WITH SCREENSHOT");
//                            loadingI.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Log.i(TAG, "onDataChange: IMAGE URL IS NULL");
                        }

                    }
                    else{
                        Log.i(TAG, "onDataChange: IMAGE DOES NOT EXISTS");
                    }
                }else{
                    Log.i(TAG, "onDataChange: THIS BRANCH DOESNOT EXIST");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ss = FirebaseDatabase.getInstance().getReference("screenshot").child(mAuth.getCurrentUser().getUid()).child("image");
        ss.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                try {
                    String imageURL = snapshot.getValue(String.class);
                    if (imageURL != null) {
                        if (!imageURL.equals("-1")) {
//                        if(!bitmap.isRecycled()) {
//                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            Glide.with(getApplicationContext())
                                    .asBitmap()
                                    .load(imageURL)
                                    .error(R.drawable.ic_error_screenshot)
//                                .apply(new RequestOptions().override(380, 230))
                                    .into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull final Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            v.vibrate(VibrationEffect.createOneShot(20, 20));
                                            screenshot_backI.setVisibility(View.INVISIBLE);
                                            screenshotI.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(GONE);
                                            loadingI.setVisibility(View.INVISIBLE);
                                            saveI.setVisibility(View.VISIBLE);
                                            screenshotI.setImage(ImageSource.bitmap(resource));
                                            Log.i(TAG, "onResourceReady: SCREENSHOT DONE");
                                            isRunning = false;

                                            saveI.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    saveImageBitmap(resource);

                                                }
                                            });
//
//                                            ss.setValue(null);
//                                            thumbnail.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if(task.isSuccessful()){
//                                                        Log.i(TAG, "onComplete: REMOVED THE THUMBNAIL");
//                                                    }
//                                                    else{
//                                                        Log.i(TAG, "onComplete: FAILED REMOVING THUMBNAIL");
//                                                    }
//                                                }
//                                            });

                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                        }

                                    });
                        } else {
                            Log.i(TAG, "onDataChange: IMAGE (SCREENSHOT) URL IS NULL");
                        }

//                    }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                    progressBar.setVisibility(GONE);
//                    loadingI.setVisibility(View.INVISIBLE);
                }
                }
                else{
                    Log.i(TAG, "onDataChange: THIS SNAPSHOT DOESNOT EXIST");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public void saveImageBitmap(Bitmap image_bitmap) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (isStoragePermissionGranted()) { // check or ask permission
            File myDir = new File(root, "/Syncopy");
            if (!myDir.exists()) {
                myDir.mkdir();
            }

            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);

            String fname = "syncopy-" + n + ".jpg";
            File file = new File(myDir, fname);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile(); // if file already exists will do nothing
                FileOutputStream out = new FileOutputStream(file);
                image_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                Toast.makeText(getApplicationContext(), "Saved to local storage", Toast.LENGTH_SHORT).show();
//                Snackbar.make(findViewById(R.id.constran_layout_snackbar), "Screenshot saved in local storage", Snackbar.LENGTH_SHORT).show();
                Snackbar snackbar = Snackbar.make(findViewById(R.id.constran_layout_snackbar), "Screenshot saved in local storage", Snackbar.LENGTH_SHORT);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                snackbar.show();
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, new String[]{file.getName()}, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!this.isFinishing ()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_status").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("status");
        mRef.setValue("-1").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "onComplete: REMOVED STATUS TO -1");
                }
            }
        });

        if(ss != null && thumbnail != null) {
            Log.i(TAG, "onDestroy: STARTS REMOVING VALUE");
            ss.setValue("-1");
            thumbnail.setValue("-1").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.i(TAG, "onComplete: REMOVED THE THUMBNAIL");
                    }
                    else{
                        Log.i(TAG, "onComplete: FAILED REMOVING THUMBNAIL");
                    }
                }
            });
            saveI.setVisibility(GONE);
        }
    }

    private class ViewPagerAdapterScreenshot extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;

        public ViewPagerAdapterScreenshot(@NonNull FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}