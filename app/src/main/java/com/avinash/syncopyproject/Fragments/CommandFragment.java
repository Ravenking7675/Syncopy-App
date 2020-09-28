package com.avinash.syncopyproject.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.avinash.syncopyproject.AddCommandActivity;
import com.avinash.syncopyproject.Model.Commands;
import com.avinash.syncopyproject.R;
import com.avinash.syncopyproject.ScreenshotActivity;
import com.avinash.syncopyproject.ScreenshotBottomSheet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;


public class CommandFragment extends Fragment {

    private View view;
    private static final String TAG = "CommandFragment";
    private TextView commandT;
    private ImageView recordI;
    private ImageView sendI;
    private Button commandListB;
    private Dialog speakDialog;
    private Vibrator vibrate;
    private Boolean longClicked=false;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private  ImageView audioI;
    private  TextView audio_statusT;
    private TextView audio_bonusT;
    private ProgressBar progressBar_speech;
    private FirebaseAuth mAuth;
    public static ScreenshotBottomSheet bottomSheet;
    public static ProgressBar progressBar_status;

    public static Boolean isRunning = false;

    public static final String NEW_COMMAND = "new command";

    public static final int RecordAudioRequestCode = 21;

    public CommandFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_command, container, false);

        final ConstraintLayout con = view.findViewById(R.id.fragmentCommandLayout);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(con);
            }
        });

        isRunning = false;

        commandT = view.findViewById(R.id.type_commandT);
        recordI = view.findViewById(R.id.recordCommandI);
        sendI = view.findViewById(R.id.sendCommandI);
        commandListB = view.findViewById(R.id.commandsBottomSheetB);
        progressBar_status = view.findViewById(R.id.progressBar_command_status);

        progressBar_status.setVisibility(View.GONE);

        vibrate = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        mAuth = FirebaseAuth.getInstance();

        sendI.setVisibility(View.INVISIBLE);
        recordI.setVisibility(View.VISIBLE);

        commandT.setBackground(getContext().getResources().getDrawable(R.drawable.text_box_rounded));

        commandT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() > 0){
                    sendI.setVisibility(View.VISIBLE);
                    recordI.setVisibility(View.INVISIBLE);

                    commandT.setBackground(getContext().getResources().getDrawable(R.drawable.text_box_rounded_selected));
                }
                else{
                    sendI.setVisibility(View.INVISIBLE);
                    recordI.setVisibility(View.VISIBLE);

                    commandT.setBackground(getContext().getResources().getDrawable(R.drawable.text_box_rounded));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        commandListB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheet = new ScreenshotBottomSheet();
                bottomSheet.isCancelable();
                bottomSheet.show(getFragmentManager(), "exampleBottomSheet");

            }
        });

        recordI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(recordI);
                if(!isRunning) {
                    if(isSpeechPermissionGranted()) {
                        showAlertDialog();
                    }
                }
                else{
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Another command is executing", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                    snackbar.show();
                }
            }
        });

        sendI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(sendI);
                if(!isRunning) {
                    String command = commandT.getText().toString().trim();
                    if (!command.isEmpty()) {
                        commandT.setText("");
                        searchForCommand(command, false);

                    } else {
                        commandT.requestFocus();
                        commandT.setError("This field can not be empty", ContextCompat.getDrawable(getContext(), R.drawable.ic_dot));
                    }
                }
                else{
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Another command is executing", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                    snackbar.show();
                }
            }
        });

            setOutputListener();

        return view;
    }

    private void hideKeyboard(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

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
                                if (status.substring(0, 1).equals("0")) {
//                                    Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Command executed successfully", Snackbar.LENGTH_SHORT).show();

                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Command executed successfully", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                                    snackbar.show();
                                }
                                if (status.substring(0, 1).equals("1")) {
//                                    Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Command does not exist", Snackbar.LENGTH_SHORT).show();

                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Command does not exist", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                                    snackbar.show();
                                }
                                if (status.substring(0, 1).equals("2")) {
//                                    Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Application is already closed", Snackbar.LENGTH_SHORT).show();

                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Application is already closed", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                                    snackbar.show();
                                }
                                    isRunning = false;
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else
                    isRunning = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    public void showAlertDialog()
    {
        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());

        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.alert_speak,
                        null);
        builder.setView(customLayout);

         audioI = customLayout.findViewById(R.id.alert_recordI);
        audio_statusT = customLayout.findViewById(R.id.alert_record_statusT);
        audio_bonusT = customLayout.findViewById(R.id.alert_record_bonusT);
        progressBar_speech = customLayout.findViewById(R.id.progressBar_alert_speech);
        progressBar_speech.setVisibility(View.GONE);

        readUserSpeech();

        audioI.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                progressBar_speech.setVisibility(View.GONE);

                if(event.getAction()  == MotionEvent.ACTION_DOWN){
                    audioI.setImageResource(R.drawable.ic_recording_on);
                    audio_statusT.setText("Listening...");
                    audio_bonusT.setVisibility(View.INVISIBLE);
                    vibrate.vibrate(VibrationEffect.createOneShot(15, 15));

                    speechRecognizer.startListening(speechRecognizerIntent);

                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    audioI.setImageResource(R.drawable.ic_recording_off);
                    audio_statusT.setText("Processing your speech...");
                    audio_statusT.setTextColor(getResources().getColor(R.color.processing_color));

                    audio_bonusT.setVisibility(View.INVISIBLE);

                    vibrate.vibrate(VibrationEffect.createOneShot(15, 15));

                    speechRecognizer.stopListening();
                }

                return true;
            }
        });

        // create and show
        // the alert dialog
        speakDialog = builder.create();
        speakDialog.show();
    }

    private void readUserSpeech() {
        Log.i(TAG, "readUserSpeech: STATS");
//        if(isSpeechPermissionGranted()){

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    Log.i(TAG, "onRmsChanged: CALLED");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {
                    audio_statusT.setText("Hold and speak the command");
                    audio_statusT.setTextColor(getResources().getColor(R.color.text_color));
                    audio_bonusT.setVisibility(View.VISIBLE);
                }

                @Override
                public void onResults(Bundle results) {
                    Log.i(TAG, "onResults: FOUND SOME MATCHES");
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if(matches != null){
                        Log.i(TAG, "onResults: SETTING TEXT VIEW");
                        audio_statusT.setText(matches.get(0));
                        audio_statusT.setTextColor(getResources().getColor(R.color.text_color));
                        audio_bonusT.setVisibility(View.INVISIBLE);
                        progressBar_speech.setVisibility(View.VISIBLE);
                        searchForCommand(matches.get(0), true);
                    }
                    else{
                        audio_statusT.setText("Hold and speak the command");
                        audio_statusT.setTextColor(getResources().getColor(R.color.text_color));
                        audio_bonusT.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });

//        }

    }

    public void searchForCommand(final String result, final Boolean isRecording) {
        progressBar_status.setVisibility(View.VISIBLE);
        if(result.length() > 6 && result.substring(0, 6).toLowerCase().equals("repeat")){
            if(isRecording){
                progressBar_speech.setVisibility(View.GONE);
                speakDialog.dismiss();
            }
            Log.i(TAG, "searchForCommand: REPEAT STARTS");
            isRunning = true;
            sendCommand(result);
        }
        else if(result.length() > 6 && result.substring(0, 5).toLowerCase().equals("close")){
            if(isRecording){
                progressBar_speech.setVisibility(View.GONE);
                speakDialog.dismiss();
                isRunning = true;
            }
            Log.i(TAG, "searchForCommand: REPEAT STARTS");
            sendCommand(result);
        }
        else {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command").child(mAuth.getCurrentUser().getUid());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String linux_command = null;
                    Boolean isExisting = false;
                    if (snapshot.exists()) {

                        for (DataSnapshot s : snapshot.getChildren()) {

                            Commands command = s.getValue(Commands.class);
                            if (command != null) {

                                if (command.getCommand_name().toLowerCase().equals(result.toLowerCase())) {
                                    isExisting = true;
                                    linux_command = command.getLinux_command();
                                }

                            }

                        }


                        if (isExisting) {
                            isRunning = true;

                            Log.i(TAG, "onDataChange: COMMAND FOUND");
                            if (isRecording) {
                                progressBar_speech.setVisibility(View.GONE);
                                speakDialog.dismiss();
//                                Toast.makeText(getContext(), "Command found, ready for processing", Toast.LENGTH_SHORT).show();
                                sendCommand(linux_command);
                            } else {

                                sendCommand(linux_command);

                            }

                        } else {

                            progressBar_status.setVisibility(View.GONE);
                            Log.i(TAG, "onDataChange: COMMAND NOT FOUND");
                            if (isRecording) {
                                progressBar_speech.setVisibility(View.GONE);
                                speakDialog.dismiss();
                                commandT.setText(result);
                            }

                            try {
//                                Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Command not found", Snackbar.LENGTH_SHORT)
//                                        .setAction("Add", new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                Intent intent = new Intent(getContext(), AddCommandActivity.class);
//                                                intent.putExtra(NEW_COMMAND, result);
//                                                startActivity(intent);
//                                            }
//                                        })
//                                        .show();

                                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.constran_layout_snackbar), "Command not found", Snackbar.LENGTH_SHORT);
                                snackbar.setAction("Add", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), AddCommandActivity.class);
                                        intent.putExtra(NEW_COMMAND, result);
                                        startActivity(intent);
                                    }
                                });
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                                snackbar.show();

                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }

                            isRunning = false;
                        }

                    }
                    else{
                        isRunning = false;
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void sendCommand(String command) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command_web").child(ScreenshotActivity.pc_uuid).child(mAuth.getCurrentUser().getUid()).child("command");
        mRef.setValue(command).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "onComplete: COMMAND SENT SUCCESSFULLY");
                }
                else{
                    Log.i(TAG, "onComplete: COMMAND SENT FAILED");
                }
            }
        });
    }


    public boolean isSpeechPermissionGranted() {
        String TAG = "Storage Permission";
        if (getContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

//            speakDialog.dismiss();

            Log.v(TAG, "Permission is granted");
            return true;
        } else {
            Log.v(TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(getActivity() , new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            return false;
        }
    }

}