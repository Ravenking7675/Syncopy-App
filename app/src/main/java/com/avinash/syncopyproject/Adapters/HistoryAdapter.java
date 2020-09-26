package com.avinash.syncopyproject.Adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Model.History;
import com.avinash.syncopyproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;

import anil.sardiwal.reboundrecycler.ReboundRecycler;

public class HistoryAdapter  extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>  {

    private static final String TAG = "HistoryAdapter";
    private Context mContext;
    private ArrayList<History> clip_hostory;
    private ConstraintLayout view;
    private Dialog dialog;
    private EditText editT;
    private ClipboardManager clipboard;
    private Dialog dialog_delete;
    private Boolean isCancellable = true;
    private FirebaseAuth mAuth;
    private LinearLayout showResponse;
    private ImageView mQRI;
    private Dialog dialog_scan;

    public HistoryAdapter(Context context, ArrayList<History> clip_hostory) {
        mContext = context;
        this.clip_hostory = clip_hostory;

        mAuth = FirebaseAuth.getInstance();
        try {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        } catch (NullPointerException e){
            //Do something

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "Profile adapter starts");

        view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.history_container, parent, false);
        ReboundRecycler.first(view);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryAdapter.ViewHolder holder, int position) {
        ReboundRecycler.bind(holder.itemView, position);

        final History history = clip_hostory.get(position);
        holder.clipT.setText(history.getClip());

        if(position%2 ==0){
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.card_color_dark));
        }
        else{
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.card_color_light));
        }

        holder.editI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
                editT.setText(history.getClip());
            }
        });

        holder.deleteI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlertDialog(history.getTime());
            }
        });

        holder.scanI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {

                    //QR is : $C+"clip data"
                    String QR_TEXT = "$C:"+history.getClip();

                    BitMatrix bitMatrix = multiFormatWriter.encode(QR_TEXT, BarcodeFormat.QR_CODE, 250, 250);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Bitmap bitmap = encoder.createBitmap(bitMatrix);
                    showScanAlertDialog();
                    mQRI.setImageBitmap(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ClipData clip = ClipData.newPlainText(null, holder.clipT.getText().toString().trim());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "Copied to your clipboard", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

    }


    @Override
    public int getItemCount() {
        return clip_hostory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView editI, scanI, deleteI;
        private TextView clipT;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            editI = itemView.findViewById(R.id.history_container_editI);
            scanI = itemView.findViewById(R.id.history_container_scanI);
            deleteI = itemView.findViewById(R.id.history_container_deleteI);
            clipT = itemView.findViewById(R.id.history_container_clipT);
            cardView = itemView.findViewById(R.id.history_container_cardV);

        }
    }

    public void showAlertDialog()
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.alert_edit,
                        null);
        builder.setView(customLayout);
        editT = customLayout.findViewById(R.id.alert_editT);
        customLayout.findViewById(R.id.alert_copyB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipData clip = ClipData.newPlainText(null, editT.getText().toString().trim());
                if(clipboard != null)
                    clipboard.setPrimaryClip(clip);

            }
        });

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.show();
    }

    public void showScanAlertDialog()
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.alert_qrcode,
                        null);
        builder.setView(customLayout);

        mQRI = customLayout.findViewById(R.id.alert_qrI);

        // create and show
        // the alert dialog
        dialog_scan = builder.create();
        dialog_scan.show();
    }

    public void showDeleteAlertDialog(final long time)
    {


        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.alert_delete_history,
                        null);
        builder.setView(customLayout);
        showResponse = customLayout.findViewById(R.id.showRemovingL_alert_history);
        customLayout.findViewById(R.id.history_alert_noI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isCancellable)
                    dialog_delete.dismiss();

            }
        });

        customLayout.findViewById(R.id.history_alert_yesI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isCancellable = false;
                showResponse.setVisibility(View.VISIBLE);
                deleteFromFirebase(time);

            }
        });


        // create and show
        // the alert dialog
        dialog_delete = builder
                .setCancelable(false)
                .create();
//        dialog_delete.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog_delete.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog_delete.getWindow().getAttributes());
        dialog_delete.getWindow().setAttributes(layoutParams);
    }

    private void deleteFromFirebase(final long time){


        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("clip").child(mAuth.getCurrentUser().getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference removeHistory = null;

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        History history = s.getValue(History.class);
                        if(history != null){

                            if(history.getTime() == time)
                                removeHistory = s.getRef();
                        }

                    }

                    if(removeHistory != null){
                        removeHistory.removeValue();
                        dialog_delete.dismiss();
                    }
                    else{
                        dialog_delete.dismiss();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
