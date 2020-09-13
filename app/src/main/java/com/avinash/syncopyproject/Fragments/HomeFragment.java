package com.avinash.syncopyproject.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.HomeAdapter;
import com.avinash.syncopyproject.CentralZoomLayoutManagerModes;
import com.avinash.syncopyproject.Model.User;
import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.Services.AutoService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.avinash.syncopyproject.SyncopyActivity.connectI;
import static com.avinash.syncopyproject.SyncopyActivity.historyI;
import static com.avinash.syncopyproject.SyncopyActivity.homeI;
import static com.avinash.syncopyproject.SyncopyActivity.profileI;


public class HomeFragment extends Fragment {

    public static final String SHARED_PREF = "user";
    public static final String SHOW_ALERT = "alert";
    public static final String USERNAME = "username";
    public static final String PHOTO = "profile photo";
    public static final String AUTO_MANUAL = "auto manual";
    public static final String MODE = "mode";
    public static final String SHORT_UUID = "short_uuid";

    private static final String TAG = "HomeFragment";
    private ImageView autoI;
    private ImageView manualI;
    private TextView autoManualT;
    private RecyclerView recyclerView;
    private ArrayList<Integer> adapter_list = new ArrayList<>();
    private HomeAdapter adapter;
    private Boolean autoF;
    private Boolean manualF;
    private TextView modeName;
    private TextView modeDetail;
    private FirebaseAuth mAuth;
    private TextView usernameT;
    private ImageView photoI;
    private SharedPreferences sharedPreferences;
    private View view;
    private ArrayList<Integer> backgroundList;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private Dialog dialog;
    private boolean showAlert = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);

//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
//
//        if(sharedPreferences.getString(USERNAME, "aj73hf73").equals("aj73hf73"))
//            startService(2,false);

        fab = getActivity().findViewById(R.id.fab);
        usernameT = view.findViewById(R.id.username_home);
        photoI = view.findViewById(R.id.profile_image_home);
        recyclerView = view.findViewById(R.id.recycler_view_home);
        autoI = view.findViewById(R.id.autoI);
        manualI = view.findViewById(R.id.manualI);
        autoManualT = view.findViewById(R.id.autoManualT);
        modeName = view.findViewById(R.id.modeName);
        modeDetail = view.findViewById(R.id.modeDetail);
        progressBar = view.findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();



        backgroundList = new ArrayList<>();

        backgroundList.add(R.drawable.ic_stealth_back2);
        backgroundList.add(R.drawable.ic_burst_back);
        backgroundList.add(R.drawable.ic_selective_back);

        final ArrayList<Integer> modes_manual = new ArrayList<>();

        modes_manual.add(R.drawable.empty_container);
        modes_manual.add(R.drawable.ic_incog_on);
        modes_manual.add(R.drawable.ic_flame_on);
        modes_manual.add(R.drawable.ic_flake_on);
        modes_manual.add(R.drawable.empty_container);
//
        final ArrayList<Integer> modes_auto = new ArrayList<>();
        modes_auto.add(R.drawable.empty_container);
        modes_auto.add(R.drawable.ic_incog_off);
        modes_auto.add(R.drawable.ic_flame_off);
        modes_auto.add(R.drawable.ic_flake_off);
        modes_auto.add(R.drawable.empty_container);


        updateUserInfo();

//        adapter_list = modes_manual;

        if(sharedPreferences.getBoolean(AUTO_MANUAL, false))
            adapter_list = modes_auto;
        else
            adapter_list = modes_manual;

        recyclerView.setHasFixedSize(true);
        final Vibrator v = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        CentralZoomLayoutManagerModes c = new CentralZoomLayoutManagerModes(getContext(), CentralZoomLayoutManagerModes.HORIZONTAL, false);
        recyclerView.setLayoutManager(c);


        adapter = new HomeAdapter(adapter_list, getContext(), progressBar);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        setCurrentItem(sharedPreferences.getInt(MODE, 2), true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    v.vibrate(VibrationEffect.createOneShot(20, 20));
                    int position = getCurrentItem();
                    if(position > 0 && position < 4 ){

                        Log.i(TAG, "MODE STORED : "+position);
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit();
                        editor.putInt(MODE, position);
                        editor.apply();

                        updateUserInfo();

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

        autoI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!autoF) {

//                    try {
//                        Log.i(TAG, "Service starting...");
//                        Intent serviceIntent = new Intent(getContext(), AutoService.class);
//                        serviceIntent.putExtra("inputExtra", "Hello This Is Avinash");
//                        getActivity().startForegroundService(serviceIntent);
//
//                    } catch (Exception e){
//                        Log.i(TAG, "onClick: EXCEPTION");
//                        e.printStackTrace();
//                    }

                    adapter_list = modes_auto;
                    adapter = new HomeAdapter(adapter_list, getContext(), progressBar);
                    recyclerView.setAdapter(adapter);
                    SharedPreferences.Editor editor = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(AUTO_MANUAL, true);
                    editor.apply();
                    if(showAlert)
                        showAlertDialog();
                    setCurrentItem(sharedPreferences.getInt(MODE, 2), true);

                    updateUserInfo();


                }
            }
        });

        manualI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!manualF) {

//                    try {
//                        Intent serviceIntent = new Intent(getContext(), AutoService.class);
//                        getActivity().stopService(serviceIntent);
//
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }

                    adapter_list = modes_manual;
                    adapter = new HomeAdapter(adapter_list, getContext(), progressBar);
                    recyclerView.setAdapter(adapter);

                    SharedPreferences.Editor editor = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(AUTO_MANUAL, false);
                    editor.apply();

                    setCurrentItem(sharedPreferences.getInt(MODE, 2), true);

                    updateUserInfo();
                }
            }
        });

        photoI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    manager.beginTransaction().replace(R.id.frame_container, new ProfileFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));

                    homeI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_home_icon));
                    connectI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_connect_icon));
                    historyI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_history_icon));
                    profileI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_profile_icon_selected));
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));

                }
                catch (NullPointerException e){
                    //Do something
                }
            }
        });

        return view;
    }

    private void startService(int mode, boolean auto_manual) {

        try {
            Log.i(TAG, "Service starting...");
            Intent serviceIntent = new Intent(getContext(), AutoService.class);
            serviceIntent.putExtra(MODE, mode);
            serviceIntent.putExtra(AUTO_MANUAL, auto_manual);
            getActivity().startForegroundService(serviceIntent);

        } catch (Exception e){
            Log.i(TAG, "onClick: EXCEPTION");
            e.printStackTrace();
        }

    }

    private void stopService() {
        try {
            Intent serviceIntent = new Intent(getContext(), AutoService.class);
            getActivity().stopService(serviceIntent);
            Log.i(TAG, "stopService: STOPED");
        }
        catch (Exception e){
            //Do something
            Log.i(TAG, "stopService: SOMETHING WENT WRONG");
        }
    }

    private void setUserDetails() {


        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user").child(mAuth.getCurrentUser().getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {

                    User user = snapshot.getValue(User.class);

                    try {
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit();
                        editor.putString(USERNAME, user.getUsername());
                        editor.putInt(PHOTO, user.getProfile_photo());
                        editor.putString(SHORT_UUID, user.getShort_id());
                        editor.apply();
                        updateUserInfo();

                    }
                    catch (Exception e) {
                        //Do something
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void showAlertDialog()
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());

        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.root_access_alert,
                        null);
        builder.setView(customLayout);
        CheckBox checkBox = customLayout.findViewById(R.id.showC);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showAlert = !isChecked;
                SharedPreferences.Editor editor = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).edit();
                editor.putBoolean(SHOW_ALERT, showAlert);
                editor.apply();
            }
        });

        customLayout.findViewById(R.id.cancelAlertB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.show();
    }

    private void updateUserInfo() {

        try {

            sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
            String username = sharedPreferences.getString(USERNAME, "");
            int profile_photo = sharedPreferences.getInt(PHOTO, R.drawable.ic_default_skull);
            boolean auto_manual = sharedPreferences.getBoolean(AUTO_MANUAL, false);
            showAlert = sharedPreferences.getBoolean(SHOW_ALERT, true);
            int mode = sharedPreferences.getInt(MODE, 2);
            startService(mode,auto_manual);

            if (username.isEmpty())
                setUserDetails();

            usernameT.setText("Hi " + username.split(" ")[0]);
            photoI.setImageResource(profile_photo);

            autoF = auto_manual;
            manualF = !auto_manual;

            if (autoF) {
                autoManualT.setText("Auto");
                autoI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_auto_text_selected));
                manualI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_manual_text));
            } else {
                autoManualT.setText("Manual");
                autoI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_auto_text));
                manualI.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_manual_text_selected));
            }

            int position = mode;

            if (autoF) {

                if (position == 1) {
                    view.setBackgroundResource(backgroundList.get(position - 1));
                    modeName.setText("Stealth Mode");
                    modeDetail.setText("Send clips to every contact without any trace");
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_stealth)));
//                progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress_stealth));
                    LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
                    Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                    Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                    backgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                    progressDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);

//                    try {
//                        stopService();
//                    }
//                    catch (Exception e) {
//                        //Do something
//                    }
                    startService(mode, autoF);


                } else if (position == 2) {
                    view.setBackgroundResource(backgroundList.get(position - 1));
                    modeName.setText("Burst Mode");
                    modeDetail.setText("Send clips to every contact");
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_burst)));
//                progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress_burst));

                    LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
                    Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                    Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                    backgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                    progressDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);

//                    try {
//                        stopService();
//                    }
//                    catch (Exception e) {
//                        //Do something
//                    }
                    startService(mode, autoF);

                } else {
                    view.setBackgroundResource(backgroundList.get(position - 1));
                    modeName.setText("Selective Mode");
                    modeDetail.setText("Send clips to only selected contacts");
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));
//                progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress_selective));

                    LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
                    Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                    Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                    backgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                    progressDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);

//                    try {
//                        stopService();
//                    }
//                    catch (Exception e) {
//                        //Do something
//                    }
                    startService(mode, autoF);

                }

            }
            else {
                if (position == 1) {
                    view.setBackgroundResource(backgroundList.get(position - 1));
                    modeName.setText("Stealth Mode");
                    modeDetail.setText("Send clips to every contact without any trace");
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_stealth)));
//                progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress_stealth));
                    LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
                    Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                    Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                    backgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                    progressDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.fab_stealth), PorterDuff.Mode.SRC_IN);

//                    try {
//                        stopService();
//                    }
//                    catch (Exception e) {
//                        //Do something
//                    }
                    startService(mode, autoF);


                } else if (position == 2) {
                    view.setBackgroundResource(backgroundList.get(position - 1));
                    modeName.setText("Burst Mode");
                    modeDetail.setText("Send clips to every contact");
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_burst)));
//                progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress_burst));

                    LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
                    Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                    Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                    backgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                    progressDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.progress_burst), PorterDuff.Mode.SRC_IN);

//                    try {
//                        stopService();
//                    }
//                    catch (Exception e) {
//                        //Do something
//                    }
                    startService(mode, autoF);

                } else {
                    view.setBackgroundResource(backgroundList.get(position - 1));
                    modeName.setText("Selective Mode");
                    modeDetail.setText("Send clips to only selected contacts");
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));
//                progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circular_progress_selective));

                    LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
                    Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                    Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                    backgroundDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                    progressDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.progress_selective), PorterDuff.Mode.SRC_IN);

//                    try {
//                        stopService();
//                    }
//                    catch (Exception e) {
//                        //Do something
//                    }
                    startService(mode, autoF);

                }
            }
        }
        catch (NullPointerException e){
            //Do something]
            setUserDetails();
        }

    }

    private int getCurrentItem() {

        return ((CentralZoomLayoutManagerModes)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

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

}