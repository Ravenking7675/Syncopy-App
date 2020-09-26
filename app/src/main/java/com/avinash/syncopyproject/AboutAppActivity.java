package com.avinash.syncopyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

public class AboutAppActivity extends AppCompatActivity {

    private static final String TAG = "AboutAppActivity";
    private TextView url1;
    private View divider1;
    private TextView url2;
    private View divider2;
    private TextView url3;
    private View divider3;
    private TextView url4;
    private View divider4;
    private TextView url5;
    private View divider5;
    private TextView url6;
    private View divider6;
    private ImageView backI;
    private NestedScrollView nestedScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        url1 = findViewById(R.id.appUrl1);
        divider1 = findViewById(R.id.app_divider1);
        url2 = findViewById(R.id.appUrl2);
        divider2 = findViewById(R.id.app_divider2);
        url3 = findViewById(R.id.appUrl3);
        divider3 = findViewById(R.id.app_divider3);
        url4 = findViewById(R.id.appUrl4);
        divider4 = findViewById(R.id.app_divider4);
        url5 = findViewById(R.id.appUrl5);
        divider5 = findViewById(R.id.app_divider5);
        url6 = findViewById(R.id.appUrl6);
        divider6 = findViewById(R.id.app_divider6);
        nestedScroll = findViewById(R.id.nested_scroll_app);

        backI = findViewById(R.id.go_back_app);

        backI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutAppActivity.this, SyncopyActivity.class);
                intent.putExtra(SyncopyActivity.FRAGMENT_NO, 4);
                startActivity(intent);
                finish();
            }
        });

        url1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider1.getBottom());

            }
        });
        url2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider2.getBottom());

            }
        });
        url3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider3.getBottom());

            }
        });
        url4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider4.getBottom());

            }
        });
        url5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider5.getBottom());

            }
        });
        url6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider6.getBottom());

            }
        });

    }

}