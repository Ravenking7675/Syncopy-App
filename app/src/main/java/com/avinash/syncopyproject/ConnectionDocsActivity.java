package com.avinash.syncopyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

public class ConnectionDocsActivity extends AppCompatActivity {

    private static final String TAG = "ConnectionDocsActivity";

    private NestedScrollView nestedScroll;
    private View divider1;
    private TextView url1T;
    private View divider2;
    private TextView url2T;
    private View divider3;
    private TextView url3T;
    private View divider4;
    private TextView url4T;
    private View divider5;
    private TextView url5T;
    private View divider6;
    private TextView url6T;
    private View divider7;
    private TextView url7T;
    private View divider8;
    private TextView url8T;
    private View divider9;
    private TextView url9T;
    private View divider10;
    private TextView url10T;
    private View divider11;
    private TextView url11T;
    private View divider12;
    private TextView url12T;
    private ImageView backI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_docs);

        url1T = findViewById(R.id.docsUrl1);
        divider1 = findViewById(R.id.divider1);
        url2T = findViewById(R.id.doc_url2);
        divider2 = findViewById(R.id.divider2);
        url3T = findViewById(R.id.doc_url3);
        divider3 = findViewById(R.id.divider3);
        url4T = findViewById(R.id.doc_url4);
        divider4 = findViewById(R.id.divider4);
        url5T = findViewById(R.id.doc_url5);
        divider5 = findViewById(R.id.divider5);
        url6T = findViewById(R.id.doc_url6);
        divider6 = findViewById(R.id.divider6);
        url7T = findViewById(R.id.doc_url7);
        divider7 = findViewById(R.id.divider7);
        url8T = findViewById(R.id.doc_url8);
        divider8 = findViewById(R.id.divider8);
        url9T = findViewById(R.id.doc_url9);
        divider9 = findViewById(R.id.divider9);
        url10T = findViewById(R.id.doc_url10);
        divider10 = findViewById(R.id.divider10);
        url11T = findViewById(R.id.doc_url11);
        divider11 = findViewById(R.id.divider11);
        url12T = findViewById(R.id.doc_url12);
        divider12 = findViewById(R.id.divider12);
        nestedScroll = findViewById(R.id.nested_scroll_docs);
        backI = findViewById(R.id.go_back_docs);

        backI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConnectionDocsActivity.this, AddCommandActivity.class));
                finish();
            }
        });

        url1T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider1.getBottom());

            }
        });

        url2T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider2.getBottom());

            }
        });

        url3T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider3.getBottom());

            }
        });

        url4T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider4.getBottom());

            }
        });

        url5T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider5.getBottom());

            }
        });

        url6T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider6.getBottom());

            }
        });

        url7T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider7.getBottom());

            }
        });

        url8T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider8.getBottom());

            }
        });

        url9T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider9.getBottom());

            }
        });

        url10T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider10.getBottom());

            }
        });

        url11T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider11.getBottom());

            }
        });
        url12T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nestedScroll.scrollTo(0, divider12.getBottom());

            }
        });




    }

}