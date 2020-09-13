package com.avinash.syncopyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.avinash.syncopyproject.Fragments.GSFirst;
import com.avinash.syncopyproject.Fragments.GSSecond;
import com.avinash.syncopyproject.Fragments.GSThird;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class WelcomeScreen extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private TabLayout tabLayout;
    private TextView nextT;
    private TextView skipT;
    private FirebaseAuth mAuth;
    private WormDotsIndicator wormDotsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        skipT = findViewById(R.id.skipT);
        nextT = findViewById(R.id.nextT);
        tabLayout = findViewById(R.id.tabLayout_ws);
        viewPager = findViewById(R.id.viewPager);
        wormDotsIndicator = (WormDotsIndicator) findViewById(R.id.worm_dots_indicator);


        fragments.add(new GSFirst());
        fragments.add(new GSSecond());
        fragments.add(new GSThird());

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
//        tabLayout.setupWithViewPager(viewPager, true);
        wormDotsIndicator.setViewPager(viewPager);
        OverScrollDecoratorHelper.setUpOverScroll(viewPager);
        nextT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewPager.setCurrentItem(getNextPossibleItemIndex(1), true);

            }
        });


        skipT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });

    }

    private void sendToLogin() {

        Intent intent = new Intent(WelcomeScreen.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private int getNextPossibleItemIndex(int i) {

        int currentIndex = viewPager.getCurrentItem();
        int total = viewPager.getAdapter().getCount();
        if (currentIndex + i < 0) {
            return 0;
        }

        return  Math.abs((currentIndex + i )% total);

    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;

        public ViewPagerAdapter(@NonNull FragmentManager fm, ArrayList<Fragment> fragments) {
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

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
            startActivity(intent);
            finishAffinity();
        }

    }
}