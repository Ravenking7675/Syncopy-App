package com.avinash.syncopyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.avinash.syncopyproject.WebViewActivity.URLCODE;

public class GithubRepoActivity extends AppCompatActivity {

    private static final String TAG = "GithubRepoActivity";
    public static final String WEBVIEWURL = "urlforwebview";
    private ImageView backI;

    private TextView androidT;
    private TextView pythonT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_github_repo);

        backI = findViewById(R.id.go_back_github);
        androidT = findViewById(R.id.androidLinkT);
        pythonT = findViewById(R.id.pythonLinkT);



        backI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GithubRepoActivity.this, SyncopyActivity.class);
                intent.putExtra(SyncopyActivity.FRAGMENT_NO, 4);
                startActivity(intent);
                finish();

            }
        });

        androidT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(GithubRepoActivity.this, WebViewActivity.class);
                intent.putExtra(WEBVIEWURL, "https://github.com/Ravenking7675/Syncopy-App");
                intent.putExtra(URLCODE, 1);
                startActivity(intent);


            }
        });

        pythonT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GithubRepoActivity.this, WebViewActivity.class);
                intent.putExtra(WEBVIEWURL, "https://github.com/Ravenking7675/Syncopy-Python");
                intent.putExtra(URLCODE, 2);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(GithubRepoActivity.this, SyncopyActivity.class);
        intent.putExtra(SyncopyActivity.FRAGMENT_NO, 4);
        startActivity(intent);
        finish();

    }

    }
