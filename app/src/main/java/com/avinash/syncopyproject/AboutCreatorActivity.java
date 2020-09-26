package com.avinash.syncopyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import static com.avinash.syncopyproject.GithubRepoActivity.WEBVIEWURL;
import static com.avinash.syncopyproject.WebViewActivity.URLCODE;

public class AboutCreatorActivity extends AppCompatActivity {

    private static final String TAG = "AboutCreatorActivity";
    private ImageView backI;
    private ImageView githubI;
    private ImageView gmailI;
    private ImageView instaI;
    private NestedScrollView nestedScroll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_creator);

        backI = findViewById(R.id.go_back_creator);
        githubI = findViewById(R.id.github_web);
        gmailI = findViewById(R.id.gmail_web);
        instaI = findViewById(R.id.insta_web);
        nestedScroll = findViewById(R.id.nested_scroll_creator);


        githubI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutCreatorActivity.this, WebViewActivity.class);
                intent.putExtra(WEBVIEWURL, "https://github.com/Ravenking7675/");
                intent.putExtra(URLCODE, 3);
                startActivity(intent);
            }
        });

        instaI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutCreatorActivity.this, WebViewActivity.class);
                intent.putExtra(WEBVIEWURL, "https://www.instagram.com/ravenking7575/");
                intent.putExtra(URLCODE, 4);
                startActivity(intent);
            }
        });

        gmailI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] recipients = {"superavinash2000@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.setType("text/html");
                intent.setPackage("com.google.android.gm");
                startActivity(intent.createChooser(intent, "Send mail"));

            }
        });

        backI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutCreatorActivity.this, SyncopyActivity.class);
                intent.putExtra(SyncopyActivity.FRAGMENT_NO, 4);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(AboutCreatorActivity.this, SyncopyActivity.class);
        intent.putExtra(SyncopyActivity.FRAGMENT_NO, 4);
        startActivity(intent);
        finish();

    }
}