package com.avinash.syncopyproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {


    // 1->github-android 2->github-python 3->Syncopy-rep 4->Instagram
    public static final String URLCODE = "urlcode";
    private static final String TAG = "WebViewActivity";
    private WebView browser;
    private ImageView backI;
    private String url;
    private ProgressBar progressBar;
    private TextView webLinkT;
    private int urlcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        try {
            urlcode = getIntent().getExtras().getInt(URLCODE);
            url = getIntent().getExtras().getString(GithubRepoActivity.WEBVIEWURL);
        }catch (Exception e){
            e.printStackTrace();
        }



        browser = findViewById(R.id.webViewAndroid);
        backI = findViewById(R.id.web_closeI);
        progressBar = findViewById(R.id.progressBar_web);
        webLinkT = findViewById(R.id.web_linkT);

        if(urlcode == 1)
            webLinkT.setText("github.com/Ravenking7675/Syncopy-App");
        if(urlcode == 2)
            webLinkT.setText("github.com/Ravenking7675/Syncopy-Python");
        if(urlcode == 3)
            webLinkT.setText("github.com/Ravenking7675");
        if(urlcode == 4)
            webLinkT.setText("instagram.com/ravenking7575");

        progressBar.setVisibility(View.VISIBLE);

        backI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=null;
                if(urlcode ==1 || urlcode == 2)
                    intent = new Intent(WebViewActivity.this, GithubRepoActivity.class);
                if(urlcode == 3 || urlcode == 4)
                    intent = new Intent(WebViewActivity.this, AboutCreatorActivity.class);
                if(urlcode == 5) {
                    intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                    intent.putExtra(SyncopyActivity.FRAGMENT_NO, 2);
                    startActivity(intent);
                    finish();
                }


                startActivity(intent);
                finish();
            }
        });

        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setLoadWithOverviewMode(true);
        browser.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.GONE);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                view.loadUrl(request.getUrl().toString());

                return true;
            }
        });
        browser.loadUrl(url);




    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch (keyCode){

                case KeyEvent.KEYCODE_BACK:
                    if(browser.canGoBack()){
                        browser.goBack();
                    }
                    else{
                        finish();
                    }
                    return true;

            }
        }

        return super.onKeyDown(keyCode, event);
    }
}