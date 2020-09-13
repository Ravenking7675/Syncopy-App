package com.avinash.syncopyproject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.avinash.syncopyproject.Fragments.ConnectFragment;
import com.avinash.syncopyproject.Fragments.HistoryFragment;
import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.Fragments.ProfileFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SyncopyActivity extends AppCompatActivity {

    public static final String USERID = "userid";
    public static ImageView homeI;
    public static ImageView connectI;
    public static ImageView historyI;
    public static ImageView profileI;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syncopy);

        homeI = findViewById(R.id.home);
        connectI = findViewById(R.id.connect);
        historyI = findViewById(R.id.history);
        profileI = findViewById(R.id.profile);
        fab = findViewById(R.id.fab);

        final Fragment fragment_home = new HomeFragment();
        final Fragment fragment_connect = new ConnectFragment();
        final Fragment fragment_history = new HistoryFragment();
        final Fragment fragment_profile = new ProfileFragment();

        switchToFragment(fragment_home);

        homeI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchToFragment(fragment_home)){
                    homeI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_home_icon_selected));
                    connectI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_connect_icon));
                    historyI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_history_icon));
                    profileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_profile_icon));
                }
            }
        });

        connectI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchToFragment(fragment_connect)){
                    homeI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_home_icon));
                    connectI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_connect_icon_selected));
                    historyI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_history_icon));
                    profileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_profile_icon));
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));

                }
            }
        });

        historyI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchToFragment(fragment_history)){
                    homeI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_home_icon));
                    connectI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_connect_icon));
                    historyI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_history_icon_selected));
                    profileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_profile_icon));
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));

                }
            }
        });

        profileI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchToFragment(fragment_profile)){
                    homeI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_home_icon));
                    connectI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_connect_icon));
                    historyI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_history_icon));
                    profileI.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_profile_icon_selected));
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_selective)));

                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean switchToFragment(Fragment fragment) {
        if(fragment != null) {
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.frame_container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            return true;
        }
        else {
            return false;
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            return;
        }
    }
}