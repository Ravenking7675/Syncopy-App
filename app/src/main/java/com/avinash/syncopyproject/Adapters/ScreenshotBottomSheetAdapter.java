package com.avinash.syncopyproject.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Fragments.CommandFragment;
import com.avinash.syncopyproject.Model.Commands;
import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.ScreenshotActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ScreenshotBottomSheetAdapter extends RecyclerView.Adapter<ScreenshotBottomSheetAdapter.ViewHolder> {

    private static final String TAG = "ScreenshotBottomSheetAd";
    private Context mContext;
    private ArrayList<Commands> commands;
    private ConstraintLayout view;
    private Dialog dialog_delete;
    private Boolean isCancellable = true;
    private LinearLayout showResponse;
    private FirebaseAuth mAuth;
    private Dialog edit_dialog;
    private ProgressBar progressBar_edit;

    public ScreenshotBottomSheetAdapter(Context context, ArrayList<Commands> commands) {
        mContext = context;
        this.commands = commands;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_screenshot_container, parent, false);
        return new ScreenshotBottomSheetAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        if(position%2 ==0){
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.card_color_dark));
        }
        else{
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.card_color_light));
        }

        holder.commandT.setText(commands.get(position).getCommand_name());

        holder.deleteI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlertDialog(commands.get(position).getCommand_code());
            }
        });

        holder.editI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showEditAlertDialog(commands.get(position).getCommand_code(), commands.get(position).getCommand_name(), commands.get(position).getLinux_command());

            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendCommand(commands.get(position).getLinux_command());
                return true;
            }
        });
    }

    private void sendCommand(String command) {
        CommandFragment.isRunning = true;
        CommandFragment.progressBar_status.setVisibility(View.VISIBLE);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_web").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("command");
        mRef.setValue(command).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "onComplete: COMMAND SENT SUCCESSFULLY");
                    CommandFragment.bottomSheet.dismiss();
                }
                else{
                    Log.i(TAG, "onComplete: COMMAND SENT FAILED");
                    CommandFragment.isRunning = false;
                }
            }
        });
    }

    private void showEditAlertDialog(final long command_code, final String command_name, final String linux_command) {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.alert_edit_command,
                        null);
        builder.setView(customLayout);
        final EditText commandNameT = customLayout.findViewById(R.id.alert_create_command_nameT);
        final EditText commandT = customLayout.findViewById(R.id.alert_create_command_commandT);
        Button saveB = customLayout.findViewById(R.id.alert_create_command_saveB);
        progressBar_edit = customLayout.findViewById(R.id.alert_progressBar_create_command);

        progressBar_edit.setVisibility(View.GONE);

        commandNameT.setText(command_name);
        commandT.setText(linux_command);

        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    String command_name = commandNameT.getText().toString().trim();
                    String command = commandT.getText().toString().trim();

                    if(command_name.isEmpty()){
                        commandNameT.requestFocus();
                        commandNameT.setError("This filed can not be empty", ContextCompat.getDrawable(mContext,R.drawable.ic_dot));
                    }
                    else if(command.isEmpty()){
                        commandT.requestFocus();
                        commandT.setError("This filed can not be empty", ContextCompat.getDrawable(mContext,R.drawable.ic_dot));
                    }
                    else {
                        updateCommand(command_code, command_name, command);
                    }
                }
        });


        // create and show
        // the alert dialog
        edit_dialog = builder
                .setCancelable(true)
                .create();
        edit_dialog.show();

    }

    private void updateCommand(final long time, final String commandName, final String commandLinux) {

        progressBar_edit.setVisibility(View.VISIBLE);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command").child(mAuth.getCurrentUser().getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference editCommand = null;
                if(snapshot.exists()){

                    for(DataSnapshot s : snapshot.getChildren()){

                        Commands command = s.getValue(Commands.class);
                        if(command != null){

                            if(command.getCommand_code() == time)
                                editCommand = s.getRef();
                        }

                    }
                    
                    if(editCommand != null){

                        Log.i(TAG, "onDataChange: FOUND COMMAND");
                        
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("command_name", commandName);
                        map.put("linux_command", commandLinux);

                        editCommand.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    edit_dialog.dismiss();
                                    Log.i(TAG, "onComplete: SUCCESS");
                                }
                                else{
                                    Toast.makeText(mContext, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Log.i(TAG, "onDataChange: FAILED NO SNAPSHOT FOUND");
                        edit_dialog.dismiss();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                        R.layout.command_delete_alert,
                        null);
        builder.setView(customLayout);
        showResponse = customLayout.findViewById(R.id.showRemovingL_alert_screenshot);
        customLayout.findViewById(R.id.screenshot_alert_noI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isCancellable)
                    dialog_delete.dismiss();

            }
        });

        customLayout.findViewById(R.id.screenshot_alert_yesI).setOnClickListener(new View.OnClickListener() {
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
        dialog_delete.show();
    }

    private void deleteFromFirebase(final long time){


        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command").child(mAuth.getCurrentUser().getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference removeCommand = null;

                if(snapshot.exists()) {

                    for(DataSnapshot s : snapshot.getChildren()) {

                        Commands command = s.getValue(Commands.class);
                        if(command != null){

                            if(command.getCommand_code() == time)
                                removeCommand = s.getRef();
                        }

                    }

                    if(removeCommand != null){
                        removeCommand.removeValue();
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

    @Override
    public int getItemCount() {
        return commands.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView editI, scanI, deleteI;
        private TextView commandT;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            editI = itemView.findViewById(R.id.screenshot_container_editI);
            scanI = itemView.findViewById(R.id.screenshot_container_scanI);
            deleteI = itemView.findViewById(R.id.screenshot_container_deleteI);
            commandT = itemView.findViewById(R.id.screenshot_container_clipT);
            cardView = itemView.findViewById(R.id.screenshot_container_cardV);

        }
    }

}
