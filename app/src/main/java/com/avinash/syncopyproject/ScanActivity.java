package com.avinash.syncopyproject;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.avinash.syncopyproject.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeActivity";
    private SurfaceView surfaceView;
    CameraSource cameraSource;
    private TextView textView;
    BarcodeDetector barcodeDetector;
    private ImageView cancelI;
    private ImageView myQrI;
    private Button searchUserB;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private int count=0;
    private FirebaseAuth mAuth;
    private Boolean isExistingContact = false;
    private Dialog dialog;
    private View customLayout;

    private TextView alertT;
    private Button alertB;
    private Button retryB;

    private ImageView tick1;
    private ImageView tick2;
    private ImageView tick3;
    private ImageView tick4;
    private ImageView tick_overflow;

    private ProgressBar progress1;
    private ProgressBar progress2;
    private ProgressBar progress3;
    private ProgressBar progress4;
    private ProgressBar progress_overflow;

    private ClipboardManager clipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Log.i(TAG, "onCreate: STARTS");

        mAuth = FirebaseAuth.getInstance();

        searchUserB = findViewById(R.id.search_by_B);
        textView = findViewById(R.id.scanText);
        myQrI = findViewById(R.id.myQRI);
        cancelI = findViewById(R.id.cancelQRI);

        if (checkPermission()) {
            //main logic or main code

            surfaceView = findViewById(R.id.camera);


            barcodeDetector = new BarcodeDetector.Builder(ScanActivity.this)
                    .setBarcodeFormats(Barcode.QR_CODE).build();

            cameraSource = new CameraSource.Builder(this, barcodeDetector)
                    .setRequestedPreviewSize(640, 480)
                    .setAutoFocusEnabled(true)
                    .build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (ActivityCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(holder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(final Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> qrCode = detections.getDetectedItems();

                    if(qrCode.size() != 0) {

                        textView.post(new Runnable() {
                            @Override
                            public void run() {

                        String code = qrCode.valueAt(0).displayValue;
//                        Log.i(TAG, "FOUND CODE : " + code);
                        if (code.length() == 9 && code.substring(0, 3).equals("$U:")) {

                            if(count < 1) {
                                Vibrator vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                vibrate.vibrate(VibrationEffect.createOneShot(10, 20));

                                findUserById(code);
                                showAlertDialog(code);

                            }
                            count++;
//
                        }

                        if(code.substring(0, 3).equals("$C:")) {

                            if (count < 1) {
                                Vibrator vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                vibrate.vibrate(VibrationEffect.createOneShot(10, 20));

                                copyToClipboard(code);
                            }
                            count++;
                        }
                    }
                        });

                    }
                }
            });

        } else {
            requestPermission();
        }

        searchUserB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchActivity();
            }
        });

        myQrI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyQRActivity.class);
                startActivity(intent);
            }
        });

        cancelI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                startActivity(intent);
            }
        });




    }

    private void copyToClipboard(String code) {


        clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, code.substring(3));
        if(clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied to your clipboard", Toast.LENGTH_SHORT).show();
        }

    }

    public void showAlertDialog(String agent_code)
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);

        // set the custom layout
        customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.scan_completing_alert,
                        null);
        builder.setView(customLayout);

        alertT = customLayout.findViewById(R.id.connect_title);
        alertB = customLayout.findViewById(R.id.continueAlertB);
        retryB = customLayout.findViewById(R.id.retryAlertB);

        tick1 = customLayout.findViewById(R.id.tick1);
        tick2 = customLayout.findViewById(R.id.tick2);
        tick3 = customLayout.findViewById(R.id.tick3);
        tick4 = customLayout.findViewById(R.id.tick4);
        tick_overflow = customLayout.findViewById(R.id.tick_overflow);

        progress1 = customLayout.findViewById(R.id.progress1);
        progress2 = customLayout.findViewById(R.id.progress2);
        progress3 = customLayout.findViewById(R.id.progress3);
        progress4 = customLayout.findViewById(R.id.progress4);
        progress_overflow = customLayout.findViewById(R.id.progress_overflow);

        alertT.setText("Ola! Agent : "+agent_code);
        alertB.setVisibility(View.GONE);
        retryB.setVisibility(View.GONE);

        setTick(progress1, tick1, true);

        alertB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                startActivity(intent);
            }
        });

        retryB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void addToFirebaseContacts(final String contact_id) {

        if(contact_id.equals("")){
            Log.i(TAG, "ALREADY EXISTS");
//            Toast.makeText(getApplicationContext(), "Contact already connected", Toast.LENGTH_SHORT).show();

            alertT.setText("Connection Already Exists");
            setTick(progress2, tick2, true);
            setTick(progress_overflow, tick_overflow, false);
            setTick(progress3, tick3, false);
            setTick(progress4, tick4, false);
            retryB.setVisibility(View.VISIBLE);

        }
        else if(contact_id.equals("overflow")){

            //Do something
            alertT.setText("Connections Limit Reached");
            setTick(progress2, tick2, true);
            setTick(progress_overflow, tick_overflow, false);
            setTick(progress3, tick3, false);
            setTick(progress4, tick4, false);
            retryB.setVisibility(View.VISIBLE);

        }
        else {
            Log.i(TAG, "addToFirebaseContacts: STARTED");
            setTick(progress2, tick2, true);
            setTick(progress3, tick3, true);

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact");
            mRef.child(mAuth.getCurrentUser().getUid()).push().setValue(contact_id).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

//                    Toast.makeText(getApplicationContext(), "Contact added successfully", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(ScanActivity.this, SyncopyActivity.class);
//                    startActivity(intent);
                    setTick(progress4, tick4, true);
                    alertB.setVisibility(View.VISIBLE);
                    alertT.setText("Connection Established");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                    setTick(progress4, tick4, false);
                    retryB.setVisibility(View.VISIBLE);
                    alertT.setText("Connection Lost");
                }
            });
            DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("contact");
            mRef2.child(contact_id).push().setValue(mAuth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        Log.i(TAG, "onComplete: SENT TO FIREBASE");
                }
            });
        }

    }

    private void checkForContactExistance(final String contact_id) {

        if(contact_id.equals("")) {
            Log.i(TAG, "USED DOES NOT EXIST");
//            Toast.makeText(getApplicationContext(), "User does not exist", Toast.LENGTH_SHORT).show();

            alertT.setText("User Does Not Exist");
            setTick(progress2, tick2, false);
            setTick(progress3, tick3, false);
            setTick(progress_overflow, tick_overflow, false);
            setTick(progress4, tick4, false);

            retryB.setVisibility(View.VISIBLE);

        }
        else if(contact_id.equals(mAuth.getCurrentUser().getUid())){
//            Toast.makeText(getApplicationContext(), "Oups! You scanned your own QR CODE", Toast.LENGTH_SHORT).show();

            alertT.setText("Oups! It's your Code");
            setTick(progress2, tick2, true);
            setTick(progress3, tick3, false);
            setTick(progress_overflow, tick_overflow, false);
            setTick(progress4, tick4, false);
            retryB.setVisibility(View.VISIBLE);

        }
        else {
            Log.i(TAG, "checkForContactExistance: STARTED");
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("contact").child(mAuth.getCurrentUser().getUid());

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {

                Boolean isExisting = false;

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i(TAG, "onDataChange: Called");
                    if (snapshot.exists()) {

                        if(snapshot.getChildrenCount() < 6) {


                            for (DataSnapshot s : snapshot.getChildren()) {
                                String userId = s.getValue(String.class);
                                if (userId.equals(contact_id)) {
                                    Log.i(TAG, "onDataChange: " + userId);
                                    Log.i(TAG, "onDataChange: " + isExisting);
                                    isExisting = true;

                                }
                                Log.i(TAG, "onDataChange: Yayyy");
                            }
                            if (!isExisting)
                                addToFirebaseContacts(contact_id);
                            else
                                addToFirebaseContacts("");

                        }
                        else{
                            addToFirebaseContacts("overflow");
                        }
                    }
                    else {
                        addToFirebaseContacts(contact_id);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void findUserById(final String shortUserID) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userid = "";
                Boolean isExisting = false;

                if(snapshot.exists()){
                    for(DataSnapshot s : snapshot.getChildren()){

                        User user = s.getValue(User.class);
                        if(user != null) {
                            Log.i(TAG, "User : " + user.getUser_id());
                            if (user.getShort_id().equals(shortUserID.substring(3))) {
                                userid = user.getUser_id();
                                isExisting = true;
                            }
                        }

                    }
                    if(!isExisting)
                        checkForContactExistance("");
                    else
                        checkForContactExistance(userid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void startSearchActivity() {

        Intent intent = new Intent(ScanActivity.this, SearchIDActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(ScanActivity.this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);

    }

    private void setTick(ProgressBar progress, ImageView tick, Boolean yes){
        if(yes){
            progress.setVisibility(View.INVISIBLE);
            tick.setVisibility(View.VISIBLE);
            tick.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_yes));
        }
        else{
            progress.setVisibility(View.INVISIBLE);
            tick.setVisibility(View.VISIBLE);
            tick.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_no));
        }
    }


    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    recreate();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getApplicationContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
