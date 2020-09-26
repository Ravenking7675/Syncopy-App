package com.avinash.syncopyproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.ScreenshotBottomSheetAdapter;
import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.avinash.syncopyproject.Model.Commands;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static android.view.View.GONE;

public class ScreenshotBottomSheet extends BottomSheetDialogFragment {

    public static final String PREF_COMMAND_LIST = "command_list";
    public static final String PREF_DEFAULT_COMMANDS = "default_commands";
    private static final String TAG = "ScreenshotBottomSheet";
    private RecyclerView recyclerView;
    private ArrayList<Commands> command_list;
    private Set<Commands> command_set = new HashSet<>();
    private ScreenshotBottomSheetAdapter  adapter;
    private ImageView defaultI;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button createCommandB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_screenshot, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_bottom_sheet_screenshot);
        defaultI = view.findViewById(R.id.screenshot_bottom_sheet_defaultI);
        progressBar = view.findViewById(R.id.bottom_sheet_progress_screenshot);
        createCommandB = view.findViewById(R.id.screenshot_bottom_sheet_doneB);

        defaultI.setVisibility(GONE);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getContext().getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager l = new LinearLayoutManager(getContext());
        l.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(l);

        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        command_list = new ArrayList<>();
//
//        command_list.add(new Commands("Open chrome", "open chrome"));
//        command_list.add(new Commands("Open spotify", "open chrome"));
//        command_list.add(new Commands("Update PC", "open chrome"));
//        command_list.add(new Commands("Open my IDE", "open chrome"));

        setUpDefaultCommandList();

        fetchCommands();

        createCommandB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), AddCommandActivity.class);
                startActivity(intent);

            }
        });

        return view;
    }

    private void fetchCommands() {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command").child(mAuth.getCurrentUser().getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                command_list = new ArrayList<>();
                command_list.clear();

                if(snapshot.exists()){

                    for(DataSnapshot s : snapshot.getChildren()){
                        Log.i(TAG, "SNAPSHOT VALUE : "+s.getChildrenCount());
                        Commands command = s.getValue(Commands.class);
                        if(command != null){

                            command_list.add(command);

                        }

                    }

                    Log.i(TAG, "FOUND COMMANDS : "+command_list.size());

                    if(command_list.size() > 0) {
                        defaultI.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter = new ScreenshotBottomSheetAdapter(getContext(), command_list);
                        recyclerView.setAdapter(adapter);
                        progressBar.setVisibility(GONE);
                    }
                    else {
                        defaultI.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(GONE);
                        progressBar.setVisibility(GONE);

                    }
                }
                else{
                    defaultI.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(GONE);
                    progressBar.setVisibility(GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setUpDefaultCommandList() {

        try{
            Boolean isFirst = sharedPreferences.getBoolean(PREF_DEFAULT_COMMANDS, true);

            if(isFirst){

                Log.i(TAG, "setUpCommandList: SENDING DEFAULT COMMANDS FOR THE FIRST TIME");

               //command > uuid > (push) Command.class

                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command").child(mAuth.getCurrentUser().getUid());

                Calendar rightNow = Calendar.getInstance();

                long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                        rightNow.get(Calendar.DST_OFFSET);
                long milliSeconds = (rightNow.getTimeInMillis() + offset) %
                        (24 * 60 * 60 * 1000);

                mRef.push().setValue(new Commands("Open chrome", "open chrome", milliSeconds)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: COMMAND 1 SENT");
                            sharedPreferences.edit().putBoolean(PREF_DEFAULT_COMMANDS, false).apply();

                        }
                    }
                });

                milliSeconds = (rightNow.getTimeInMillis() + offset) %
                        (24 * 60 * 60 * 1000);

                mRef.push().setValue(new Commands("Open spotify", "open chrome", milliSeconds)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: COMMAND 2 SENT");
                            sharedPreferences.edit().putBoolean(PREF_DEFAULT_COMMANDS, false).apply();

                        }
                    }
                });

                milliSeconds = (rightNow.getTimeInMillis() + offset) %
                        (24 * 60 * 60 * 1000);

                mRef.push().setValue(new Commands("Update PC", "open chrome", milliSeconds)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: COMMAND 3 SENT");
                            sharedPreferences.edit().putBoolean(PREF_DEFAULT_COMMANDS, false).apply();

                        }
                    }
                });

                milliSeconds = (rightNow.getTimeInMillis() + offset) %
                        (24 * 60 * 60 * 1000);

                mRef.push().setValue(new Commands("Open my IDE", "open chrome", milliSeconds)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: COMMAND 4 SENT");
                            sharedPreferences.edit().putBoolean(PREF_DEFAULT_COMMANDS, false).apply();

                        }
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
