package com.avinash.syncopyproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.avinash.syncopyproject.Fragments.HomeFragment;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

public class BottomSheetProfile  extends BottomSheetDialogFragment {

    private static final String TAG = "BottomSheetProfile";
    private Dialog dialog;
    private Boolean isCancellable = true;
    private ImageView logoutI;
    private ImageView aboutCreatorI;
    private ImageView aboutAppI;
    private ImageView githubI;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.logout_bottom_sheet, container, false);

        logoutI = view.findViewById(R.id.logout_text_bottomSheetI);
        aboutCreatorI = view.findViewById(R.id.about_the_creatorI);
        aboutAppI = view.findViewById(R.id.about_the_appI);
        githubI = view.findViewById(R.id.github_repoI);

        aboutAppI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AboutAppActivity.class);
                startActivity(intent);
            }
        });

        aboutCreatorI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AboutCreatorActivity.class);
                startActivity(intent);
            }
        });

        githubI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GithubRepoActivity.class);
                startActivity(intent);
            }
        });




        logoutI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertLogoutDialog();

            }
        });

        return view;
    }

    public void showAlertLogoutDialog()
    {


        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());

        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.logout_alert,
                        null);
        builder.setView(customLayout);

        customLayout.findViewById(R.id.logout_alert_noI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCancellable)
                    dialog.dismiss();
            }
        });

        customLayout.findViewById(R.id.logout_alert_yesI).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancellable = false;
                customLayout.findViewById(R.id.logout_showRemovingL).setVisibility(View.VISIBLE);
                logoutUser();
            }
        });

        // create and show
        // the alert dialog
        dialog = builder
                .setCancelable(false)
                .create();
        dialog.show();
    }

    private void logoutUser() {


        try {
            getContext().getSharedPreferences(HomeFragment.SHARED_PREF, Context.MODE_PRIVATE).edit().clear().apply();

            Log.i(TAG, "logoutUser: LOGGING OUT");

            //firebase logout
            FirebaseAuth.getInstance().signOut();
            Log.i(TAG, "logoutUser: FIREBASE LOGOUT DONE");
            //facebook logout
            try {
                if(LoginManager.getInstance() != null) {
                    LoginManager.getInstance().logOut();
                    Log.i(TAG, "logoutUser: FACEBOOK LOGOUT DONE");
                }
            }catch (Exception e){
                //Do something
                e.printStackTrace();
            }

            //google logout
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
                googleSignInClient.signOut();
                Log.i(TAG, "logoutUser: GOOGLE LOGOUT DONE");
            }
            catch (Exception e){

                //Do something
                e.printStackTrace();
            }
            dialog.dismiss();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            getActivity().finish();
            startActivity(intent);

        }catch (Exception e){
            //Do something
            e.printStackTrace();
        }

    }

}
