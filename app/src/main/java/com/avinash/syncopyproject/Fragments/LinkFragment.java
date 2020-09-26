package com.avinash.syncopyproject.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.ScreenshotActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LinkFragment extends Fragment {

    private static final String TAG = "LinkFragment";
    private View view;
    private Spinner spinner;
    private TextView urlT;
    private ImageView sendI;
    private ImageView chromeI;
    private ImageView chromiumI;
    private ImageView firefoxI;
    private int selected_browser = 0;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar_status;

    public LinkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_link, container, false);

        spinner = view.findViewById(R.id.spinnerBrowser);
        urlT = view.findViewById(R.id.type_urlT);
        sendI = view.findViewById(R.id.sendUrlI);
        chromeI = view.findViewById(R.id.chromeI);
        chromiumI = view.findViewById(R.id.chromiumI);
        firefoxI = view.findViewById(R.id.firefoxI);
        progressBar_status = view.findViewById(R.id.progressBar_command_link);

        final ConstraintLayout con = view.findViewById(R.id.fragment_link_layout);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(con);
            }
        });

        progressBar_status.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0) {
                    chromiumI.setVisibility(View.VISIBLE);
                    chromeI.setVisibility(View.GONE);
                    firefoxI.setVisibility(View.GONE);
                    selected_browser = 0;
                }
                else if(position == 1){
                    chromiumI.setVisibility(View.GONE);
                    chromeI.setVisibility(View.VISIBLE);
                    firefoxI.setVisibility(View.GONE);
                    selected_browser = 1;
                }
                else{
                    chromiumI.setVisibility(View.GONE);
                    chromeI.setVisibility(View.GONE);
                    firefoxI.setVisibility(View.VISIBLE);
                    selected_browser = 2;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        urlT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() > 0){
                    urlT.setBackground(getContext().getResources().getDrawable(R.drawable.text_box_rounded_selected));
                }
                else{
                    urlT.setBackground(getContext().getResources().getDrawable(R.drawable.text_box_rounded));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(sendI);
                if (!CommandFragment.isRunning) {

                    String link;
                    String url = urlT.getText().toString().trim();
                    if (url.isEmpty()) {

                        urlT.requestFocus();
                        urlT.setError("This field can not be empty", ContextCompat.getDrawable(getContext(), R.drawable.ic_dot));

                    } else {
                        if (selected_browser == 0)
                            link = "link:*chromium*" + url;
                        else if (selected_browser == 1)
                            link = "link:*chrome*" + url;
                        else
                            link = "link:*firefox*" + url;
                        urlT.setText("");
                        sendCommand(link);

                    }

                }
                else{
//                    Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Another command is running", Snackbar.LENGTH_SHORT).show();
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Another command is running", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                    snackbar.show();
                }
            }
        });

        setOutputListener();

        return view;
    }

    private void setOutputListener() {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_status").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("status");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    String status = snapshot.getValue(String.class);
                    if(status != null){
                        if(!status.equals("-1")) {
                            progressBar_status.setVisibility(View.GONE);
                            try {
                                if (status.substring(0, 1).equals("1")) {
//                                    Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "This browser is not installed", Snackbar.LENGTH_SHORT).show();
                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "This browser is not installed", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                                    snackbar.show();
                                }
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                            CommandFragment.isRunning = false;
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void hideKeyboard(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void sendCommand(String command) {
        progressBar_status.setVisibility(View.VISIBLE);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_web").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("command");
        mRef.setValue(command).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "onComplete: COMMAND SENT SUCCESSFULLY");

                }
                else{
                    Log.i(TAG, "onComplete: COMMAND SENT FAILED");
                    CommandFragment.isRunning = false;
                }
            }
        });
    }

}