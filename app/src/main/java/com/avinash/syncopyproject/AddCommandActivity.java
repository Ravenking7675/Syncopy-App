package com.avinash.syncopyproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.avinash.syncopyproject.Fragments.CommandFragment;
import com.avinash.syncopyproject.Model.Commands;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddCommandActivity extends AppCompatActivity {

    private static final String TAG = "AddCommandActivity";
    private EditText commandT;
    private EditText commandNameT;
    private Button saveB;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private ImageView backI;
    private TextView commandDocsT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_command);

        commandT = findViewById(R.id.create_command_commandT);
        commandNameT = findViewById(R.id.create_command_nameT);
        saveB = findViewById(R.id.create_command_saveB);
        progressBar = findViewById(R.id.progressBar_create_command);
        commandDocsT = findViewById(R.id.comamnd_docsT);
//        backI = findViewById(R.id.create_command_backI);

        commandDocsT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(AddCommandActivity.this, ConnectionDocsActivity.class));
//                overridePendingTransition(R.anim.no_animation, R.anim.slide_down);


            }
        });

        final ConstraintLayout con = findViewById(R.id.snack_bar_add_command);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(con);
            }
        });

        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        try{

            String command_name = getIntent().getExtras().getString(CommandFragment.NEW_COMMAND);
            commandNameT.setText(command_name);
            commandT.requestFocus();

        }catch (Exception e){
            e.printStackTrace();
        }

        commandNameT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() > 0){

                    commandNameT.setBackground(getResources().getDrawable(R.drawable.text_box_selected));
                }
                else{
                    commandNameT.setBackground(getResources().getDrawable(R.drawable.text_box));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
        commandT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() > 0){

                    commandT.setBackground(getResources().getDrawable(R.drawable.text_box_selected));
                }
                else{
                    commandT.setBackground(getResources().getDrawable(R.drawable.text_box));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(saveB);
                saveCommand();
            }
        });


//        backI.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(getApplicationContext(), ScreenshotActivity.class);
//                startActivity(intent);
//
//            }
//        });

    }
    private void hideKeyboard(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
    private void saveCommand() {

        //command > uuid > (push) Command.class

        String command_name = commandNameT.getText().toString().trim();
        String command = commandT.getText().toString().trim();

        if(command_name.isEmpty()){
            commandNameT.requestFocus();
            commandNameT.setError("This filed can not be empty", ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_dot));
        }
        else if(command.isEmpty()){
            commandT.requestFocus();
            commandT.setError("This filed can not be empty", ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_dot));
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            Calendar rightNow = Calendar.getInstance();

            long offset = rightNow.get(Calendar.ZONE_OFFSET) +
                    rightNow.get(Calendar.DST_OFFSET);
            long milliSeconds = (rightNow.getTimeInMillis() + offset) %
                    (24 * 60 * 60 * 1000);

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("command").child(mAuth.getCurrentUser().getUid());

            mRef.push().setValue(new Commands(command_name, command, milliSeconds)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressBar.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(), "Command saved", Toast.LENGTH_SHORT).show();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.snack_bar_add_command), "Command saved", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                        snackbar.show();
                    }
                    else{
                        progressBar.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.snack_bar_add_command), "Something went wrong, please try again", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color));
                        snackbar.show();
                    }
                }
            });

        }

    }
}