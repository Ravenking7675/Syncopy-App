package com.avinash.syncopyproject.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Model.PcUser;
import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.ScreenshotActivity;
import com.avinash.syncopyproject.SyncopyActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import anil.sardiwal.reboundrecycler.ReboundRecycler;

import static com.avinash.syncopyproject.Fragments.ConnectFragment.connection_ids;
import static com.avinash.syncopyproject.Fragments.ConnectFragment.pc_users;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ViewHolder> {

    public static final String INTENT_PC_UUID = "pc_uuid_intent";
    public static final String INTENT_PC_NAME = "pc_name_intent";
    public static final String INTENT_PC_TYPE = "pc_type_intent";

    private static final String TAG = "ConnectionAdapter";
    private Context mContext;
    private ArrayList<PcUser> connections;
    private SharedPreferences sharedPreferences;
    private ConstraintLayout view;
    private ArrayList<PcUser> connections_2;
    private Dialog dialog;
    private Boolean isCancellable = true;

    public ConnectionAdapter(Context context, ArrayList<PcUser> connections) {
        mContext = context;
        this.connections = connections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.connections_container, parent, false);
        ReboundRecycler.first(view);
        return new ConnectionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        ReboundRecycler.bind(holder.itemView, position);

        try {
            if (connections.get(position).getPcType().toLowerCase().equals("linux")) {
                holder.backgroundI.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_linux_card));
                holder.pcTypeT.setText("Linux");
                holder.pcNameT.setText(connections.get(position).getPcName());
                holder.pcIDT.setText(connections.get(position).getUuid());
            } else {
                holder.backgroundI.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_windows_card));
                holder.pcTypeT.setText("Windows");
                holder.pcNameT.setText(connections.get(position).getPcName());
                holder.pcIDT.setText(connections.get(position).getUuid());
            }

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (connections.get(position).getPcType().toLowerCase().equals("linux")) {

                        Intent intent = new Intent(mContext, ScreenshotActivity.class);
                        intent.putExtra(INTENT_PC_NAME, connections.get(position).getPcName());
                        intent.putExtra(INTENT_PC_UUID, connections.get(position).getUuid());
                        // 1->Linux 2->Windows
                        intent.putExtra(INTENT_PC_TYPE, 1);
                        mContext.startActivity(intent);
                    }
                }
            });

            holder.card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showDeleteAlertDialog(connections.get(position).getUuid(), connections.get(position).getPcName());

                    return true;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void showDeleteAlertDialog(final String uuid, String pc_name)
    {


        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.delete_pc_user_alert,
                        null);
        builder.setView(customLayout);
        final LinearLayout showResponse = customLayout.findViewById(R.id.showRemovingL_alert_connection_adapter);
        TextView removeT = customLayout.findViewById(R.id.remove_connection_text);
        removeT.setText("Remove "+pc_name+"?");
        customLayout.findViewById(R.id.connection_adapter_alert_noI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isCancellable)
                    dialog.dismiss();

            }
        });

        customLayout.findViewById(R.id.connection_adapter_alert_yesI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isCancellable = false;
                showResponse.setVisibility(View.VISIBLE);
                if(pc_users.contains(uuid)) {

                    pc_users.remove(uuid);
                    connection_ids = new ArrayList<>(pc_users);
                    Log.i(TAG, "connection id : "+connection_ids.size());

                    setPcToKill(uuid);


                }

            }
        });


        // create and show
        // the alert dialog
        dialog = builder
                .setCancelable(false)
                .create();
        dialog.show();
    }

    private void setPcToKill(String uuid) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user_web").child(uuid);
        Map<String, Object> map = new HashMap<>();
        map.put("connectedTo", "-1");
        mRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Log.i(TAG, "onComplete: REMOVED PC USER SUCCESSFULLY");
                    Intent intent = new Intent(mContext, SyncopyActivity.class);
                    intent.putExtra(SyncopyActivity.FRAGMENT_NO, 2);
                    mContext.startActivity(intent);
                }
                else{
                    Log.i(TAG, "onComplete: FAILED TO REMOVE PC USER");
                    Toast.makeText(mContext, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView card;
        ImageView backgroundI;
        TextView pcTypeT, pcNameT, pcIDT;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card_connection);
            backgroundI = itemView.findViewById(R.id.connection_container_backgroundI);
            pcTypeT = itemView.findViewById(R.id.connection_container_typeT);
            pcNameT = itemView.findViewById(R.id.connection_container_name);
            pcIDT = itemView.findViewById(R.id.connection_container_pcidT);

        }
    }

}
