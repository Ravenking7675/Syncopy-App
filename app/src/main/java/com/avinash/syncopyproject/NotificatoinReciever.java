package com.avinash.syncopyproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificatoinReciever extends BroadcastReceiver {

    private static final String TAG = "NotificatoinReciever";

    private String prevClip = "";
    private String newClip = "*&%*%*@#";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "onReceive: GOT A CLIP");
        String message = intent.getStringExtra("message");
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

    }

    }
