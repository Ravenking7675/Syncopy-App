package com.avinash.syncopyproject.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.avinash.syncopyproject.Adapters.BottomSheetAdapter;
import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.Model.History;
import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.SyncopyActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import static com.avinash.syncopyproject.Fragments.HomeFragment.SHARED_PREF;
import static com.avinash.syncopyproject.NotificationChannelCreator.CHANNEL_ID;

public class AutoService extends Service {

    public static final String PREV_CLIP = "prev";
    public static final String CONNECTION_LIST = "connections_list";

    private static final String TAG = "AutoService";
    boolean isRunning = false;

    private int mode = 2;
    private boolean auto_manual = false;
    public static ClipData sharedClip;

    private HashSet<String> connection_set = new HashSet<>();;
    private HashSet<String> default_set = new HashSet<>();
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: START");
        super.onCreate();

        new Thread(new Runnable() {
            @Override
            public void run() {
                listenToClipChange();
            }
        }).start();

        mAuth = FirebaseAuth.getInstance();

        default_set.add(mAuth.getCurrentUser().getUid());



    }

    private void collectConnectionData() {
        try {
            connection_set.add(mAuth.getCurrentUser().getUid());
        }catch (Exception e){
            e.printStackTrace();
        }
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                connection_set.clear();
                connection_set = new HashSet<>();
                try {
                    connection_set.add(mAuth.getCurrentUser().getUid());
                }catch (Exception e){
                    e.printStackTrace();
                }
                SharedPreferences.Editor sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).edit();

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()){

                        String contact_id = s.getValue(String.class);
                        if(contact_id != null){

                            connection_set.add(contact_id);
                            sharedPreferences.putStringSet(CONNECTION_LIST, connection_set).apply();
                            Log.i(TAG, "FOUND USERS IN CONTACT : "+contact_id);

                        }

                    }

                }
                else{
                    Log.i(TAG, "NO CONNECTION FOUND");
                    sharedPreferences.putStringSet(CONNECTION_LIST, connection_set).apply();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: START");

        //1-Stealth 2-Burst 3-Selective
        mode = intent.getIntExtra(HomeFragment.MODE, 2);

        //true-Auto false-Manual
        auto_manual = intent.getBooleanExtra(HomeFragment.AUTO_MANUAL, false);

        //icon, mode name, auto/manual, secondary text

        if(auto_manual){

            if(mode == 1){
                createNotification(R.drawable.ic_incog_notification_off, "Stealth", "Auto", "Tap here to auto send clips in stealth mode", R.color.green);
            }
            else if(mode == 2){
                createNotification(R.drawable.ic_flame_notification_off, "Burst", "Auto", "Tap here to auto send clips in burst mode", R.color.green);
            }
            else{
                createNotification(R.drawable.ic_flake_notification_off, "Selective", "Auto", "Tap here to auto send clips in selective mode", R.color.green);
            }


        }

        else{
            if(mode == 1){
                createNotification(R.drawable.ic_incog_notification, "Stealth", "Manual", "Tap here to send clips in stealth mode", R.color.progress_stealth);
            }
            else if(mode == 2){
                createNotification(R.drawable.ic_flame_notification, "Burst", "Manual", "Tap here to send clips in burst mode", R.color.fab_burst);
            }
            else{
                createNotification(R.drawable.ic_flake_notification, "Selective", "Manual", "Tap here to send clips in selective mode", R.color.fab_selective);
            }
        }


        return START_NOT_STICKY;
    }



    private void listenToClipChange(){

            collectConnectionData();

            listenClipsFromOtherContacts();
            listenClipsFromPC();

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
            String prevClip = "";
            String newClip = "";

            prevClip = sharedPreferences.getString(PREV_CLIP, "");

            while (!isRunning) {
                if(auto_manual) {
                    try {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        ClipData c = clipboard.getPrimaryClip();

                        newClip = c.getItemAt(0).getText().toString().trim();
                        if (!sharedPreferences.getString(PREV_CLIP, "").equals(newClip)) {

//                            if(!sharedClip.getItemAt(0).getText().toString().trim().equals(newClip)) {
                                Log.i(TAG, "listenToClipChange: YOU CREATED THIS CLIP BUDDY");
//                                prevClip = newClip;
                                sharedPreferences.edit().putString(PREV_CLIP, newClip).apply();
                                Log.i(TAG, "listenToClipChange: SENDING >>> : " + newClip);
                                sendToFirebase(newClip);
//                            }
                        }


                    } catch (Exception e) {
//                e.printStackTrace();
                    }
                }
            }
        }

    private void listenClipsFromPC() {

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        try {
            FirebaseDatabase.getInstance().getReference("clip_web").child(mAuth.getCurrentUser().getUid()).child("message")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                    History history = snapshot.getValue(History.class);
                                    if (history != null) {

                                        try {

                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                                            Log.i(TAG, "GOT CLIP : " + history.getClip());

                                            if (history.getClip() != null) {

                                                Log.i(TAG, "COPYING TO CLIPBOARD");
                                                sharedClip = ClipData.newPlainText(null, history.getClip());
                                                clipboard.setPrimaryClip(sharedClip);
                                                saveToHistory(history.getClip());
                                                try {
                                                    sharedPreferences.edit().putString(PREV_CLIP, history.getClip()).apply();
                                                } catch (Exception e) {
                                                    Log.i(TAG, "FAILED TO UPDATE SHARED PREF");
                                                }
                                            } else {
                                                Log.i(TAG, "CLIP IS SAME SO NOT COPYING");
                                            }

                                        } catch (Exception e) {
                                            //Do something
                                            Log.i(TAG, "CLIP DATA ERROR");
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

    private void listenClipsFromOtherContacts() {
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        try {
            FirebaseDatabase.getInstance().getReference("clip").child(mAuth.getCurrentUser().getUid()).limitToLast(1)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                for (DataSnapshot s : snapshot.getChildren()) {
                                    History history = s.getValue(History.class);
                                    if (history != null) {

                                        try {

                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                                            Log.i(TAG, "GOT CLIP : " + history.getClip());

                                            if (history.getClip() != null) {

                                                Log.i(TAG, "COPYING TO CLIPBOARD");
                                                sharedClip = ClipData.newPlainText(null, history.getClip());
                                                clipboard.setPrimaryClip(sharedClip);
                                                try {
                                                    sharedPreferences.edit().putString(PREV_CLIP, history.getClip()).apply();
                                                } catch (Exception e) {
                                                    Log.i(TAG, "FAILED TO UPDATE SHARED PREF");
                                                }
                                            } else {
                                                Log.i(TAG, "CLIP IS SAME SO NOT COPYING");
                                            }

                                        } catch (Exception e) {
                                            //Do something
                                            Log.i(TAG, "CLIP DATA ERROR");
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

    private void sendToFirebase(String clip) {

        Log.i(TAG, "Inside with mode : "+mode);
        Calendar rightNow = Calendar.getInstance();

        long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                rightNow.get(Calendar.DST_OFFSET);
        long milliSeconds = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000);

        HashMap<String, Object> map = new HashMap<>();
        map.put("time", milliSeconds);
        map.put("clip", clip);

        if(mode == 1)
            map.put("history", false);
        else
            map.put("history", true);

        try {

            if (mode == 1 || mode == 2) {

                Log.i(TAG, "SHARED PREF DATA : "+getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(CONNECTION_LIST, connection_set).size());
                Log.i(TAG, "SHARED PREF DATA AS STRING : "+getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(CONNECTION_LIST, connection_set).toString());
                for (final String userID : getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(CONNECTION_LIST, connection_set)) {
                    Log.i(TAG, "SHARED PREF SIZE : "+getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(CONNECTION_LIST, connection_set).size());
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("clip").child(userID);

                    mRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: SUCCESSFULLY SENT");
                                try {
                                    if (userID.equals(mAuth.getCurrentUser().getUid()))
                                        Toast.makeText(AutoService.this, "clip send successfully", Toast.LENGTH_SHORT).show();
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, "FAILED TO SEND CLIP : " + task.getException());
                            }
                        }
                    });
                }

            }
            else{
                Log.i(TAG, "SHARED PREF DATA MANUAL : "+getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(BottomSheetAdapter.SELECTIVE_CONTACTS, default_set).size());

                for (final String userID : getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE).getStringSet(BottomSheetAdapter.SELECTIVE_CONTACTS, default_set)) {

                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("clip").child(userID);

                    mRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, "onComplete: SUCCESSFULLY SENT");
                                try {
                                    if (userID.equals(mAuth.getCurrentUser().getUid()))
                                        Toast.makeText(getApplicationContext(), "clip send successfully", Toast.LENGTH_SHORT).show();
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, "FAILED TO SEND CLIP : " + task.getException());
                            }
                        }
                    });
                }

            }
        }
        catch (NullPointerException e ){
            //Do something
        }
    }

    private void saveToHistory(String clip){

        Calendar rightNow = Calendar.getInstance();

        long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                rightNow.get(Calendar.DST_OFFSET);
        long milliSeconds = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000);

        HashMap<String, Object> map = new HashMap<>();
        map.put("time", milliSeconds);
        map.put("clip", clip);
        map.put("history", true);

        DatabaseReference mRef = null;
        try {
            mRef = FirebaseDatabase.getInstance().getReference("clip").child(mAuth.getCurrentUser().getUid());

            mRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onComplete: SUCCESSFULLY SENT");
                        try {
                            Log.i(TAG, "onComplete : SENT PC CLIP TO FIREBASE");
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    } else {
                        Log.i(TAG, "FAILED TO SEND CLIP : " + task.getException());
                    }
                }
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    //icon, mode name, auto/manual, secondary text
    private void createNotification(int icon, String name, String auto_manual, String secondText, int color) {

        Intent notificationIntent = new Intent(this, SyncopyActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(name)
                .setContentText(secondText)
                .setSubText(String.format("(%s)", auto_manual))
                .setSmallIcon(icon)
                .setColor(getResources().getColor(color))
                .setContentIntent(pendingIntent)
                .setVibrate(null)
                .setSound(null)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .build();

        synchronized (notification){
            notification.notify();
        }

        startForeground(1, notification);


    }

    @Override
    public void onDestroy() {
        isRunning = true;
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
