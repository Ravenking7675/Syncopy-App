package com.avinash.syncopyproject.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.Services.AutoService;

public class ConnectFragment extends Fragment {

    private static final String TAG = "ConnectFragment";
    private Button startB;
    private Button endB;
    private View view;

    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_connect, container, false);

        startB = view.findViewById(R.id.serviceStartB);
        endB = view.findViewById(R.id.serviceEndB);

        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.i(TAG, "Service starting...");
                    Intent serviceIntent = new Intent(getContext(), AutoService.class);
                    serviceIntent.putExtra("inputExtra", "Hello This Is Avinash");
                    getActivity().startForegroundService(serviceIntent);
                }
                catch (Exception e){
                    Log.i(TAG, "onClick: EXCEPTION");
                    e.printStackTrace();
                }
            }
        });


        endB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getContext(), AutoService.class);
                getActivity().stopService(serviceIntent);
            }
        });

        return view;
    }
}