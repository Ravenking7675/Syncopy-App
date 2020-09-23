package com.avinash.syncopyproject.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avinash.syncopyproject.Adapters.ConnectionAdapter;
import com.avinash.syncopyproject.Model.PcUser;
import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.SearchIDActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import anil.sardiwal.reboundrecycler.ReboundRecycler;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ConnectFragment extends Fragment {

    private static final String TAG = "ConnectFragment";
    private View view;
    private SharedPreferences sharedPreferences;
    private Set<String> pc_users = new HashSet<>();
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ConnectionAdapter adapter;
    private ArrayList<PcUser> connections = new ArrayList<>();
    private ArrayList<String> connection_ids;
    private ProgressBar progressBar;
    private ImageView defaultI;
    private TextView defaultT;
    private LinearLayout defaultParaT;

    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_connect, container, false);

        recyclerView = view.findViewById(R.id.connection_recycler_view);
        defaultI = view.findViewById(R.id.connections_defaultI);
        defaultT = view.findViewById(R.id.connection_defaultT);
        defaultParaT = view.findViewById(R.id.connection_default_ParaT);
        progressBar = view.findViewById(R.id.progressBar_connect_loading);

        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = getContext().getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE);

        pcUserQuitListener();

        recyclerView.setHasFixedSize(true);

        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager l = new LinearLayoutManager(getContext());
        l.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(l);

        try{

            connection_ids = new ArrayList<>(sharedPreferences.getStringSet(SearchIDActivity.PREF_PC_USERS, null));
            Log.i(TAG, "Connection ids : "+connection_ids.size());
        }catch (Exception e){
            e.printStackTrace();
        }

        updateConnectionsInfo();

        //Libraries
        ReboundRecycler.init(recyclerView);
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        return view;
    }

    private void updateConnectionsInfo() {

        if(connection_ids == null){
            defaultI.setVisibility(View.VISIBLE);
            defaultT.setVisibility(View.VISIBLE);
            defaultParaT.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

        }
        else if(connection_ids.size() == 0){

            defaultI.setVisibility(View.VISIBLE);
            defaultT.setVisibility(View.VISIBLE);
            defaultParaT.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
        else {

            defaultI.setVisibility(View.GONE);
            defaultT.setVisibility(View.GONE);
            defaultParaT.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            Log.i(TAG, "updateConnectionsInfo: Starts");
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user_web");
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    connections = new ArrayList<>();
                    connections.clear();
//                connection_ids = new ArrayList<>(sharedPreferences.getStringSet(SearchIDActivity.PREF_PC_USERS, null));
                    Log.i(TAG, "onDataChange: Connection IDs " + connection_ids.size());
                    if (snapshot.exists()) {

                        for (DataSnapshot s : snapshot.getChildren()) {

                            PcUser user = s.getValue(PcUser.class);
                            if (user != null) {
//                            Log.i(TAG, "onDataChange: USER ID : "+user.getUuid());
                                if (connection_ids.contains(user.getUuid())) {
                                    Log.i(TAG, "CONNECTION FOUND");
                                    connections.add(user);

                                }
                            }
                        }
                        Log.i(TAG, "CONNECTIONS ARE : " + connections.size());

                        adapter = new ConnectionAdapter(getContext(), connections);
                        recyclerView.setAdapter(adapter);
                        progressBar.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void pcUserQuitListener() {

        try{
            progressBar.setVisibility(View.VISIBLE);
            pc_users = sharedPreferences.getStringSet(SearchIDActivity.PREF_PC_USERS, new HashSet<String>());

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("status");

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){

                        for(DataSnapshot s : snapshot.getChildren()) {

                            if(pc_users.contains(s.getKey())) {
                                Boolean status = s.getValue(Boolean.class);
                                if (!status) {
                                    pc_users.remove(s.getKey());
                                    connection_ids = new ArrayList<>(pc_users);
                                    Log.i(TAG, "connection id : "+connection_ids.size());
                                    updateConnectionsInfo();
                                    Log.i(TAG, "REMOVING USER "+s.getKey());
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
}