package com.avinash.syncopyproject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MyQRActivity extends AppCompatActivity {

    private static final String TAG = "MyQRActivity";

    private ImageView myQRI;
    private ImageView cancelI;
    private Button scanActivityB;
    private SharedPreferences sharedPreferences;
    private String short_uuid;
    private TextView userIdT;
    private ClipboardManager clipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_q_r);

        scanActivityB = findViewById(R.id.open_scannerB);
        cancelI = findViewById(R.id.myqr_cancelI);
        myQRI = findViewById(R.id.qrImage);
        userIdT = findViewById(R.id.userIdT);

        sharedPreferences = getSharedPreferences(HomeFragment.SHARED_PREF, MODE_PRIVATE);
        short_uuid = sharedPreferences.getString(HomeFragment.SHORT_UUID, "Unknown");
        userIdT.setText(short_uuid);

        clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {

            //QR is : reverse_uuid+short_uuid+reverse_uuid+short_uuid
            String QR_TEXT = "$U:"+short_uuid;

            BitMatrix bitMatrix = multiFormatWriter.encode(QR_TEXT, BarcodeFormat.QR_CODE, 250, 250);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            myQRI.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        cancelI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
                startActivity(intent);
            }
        });

        scanActivityB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                startActivity(intent);
            }
        });

        userIdT.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ClipData clip = ClipData.newPlainText(null, userIdT.getText().toString().trim());
                if(clip != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MyQRActivity.this, "Copied to your clipboard", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

    }
}