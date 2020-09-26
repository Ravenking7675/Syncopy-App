package com.avinash.syncopyproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextView signupT;
    private ImageView googleI;
    private ImageView facebookI;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private static final int RC_SIGN_IN = 123;
    private static final String EMAIL = "email";
    private EditText emailT;
    private EditText passwordT;
    private Button loginB;
    private AlertDialog dialog;
    private TextView forgotPassT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signupT = findViewById(R.id.signupT);
        googleI = findViewById(R.id.googleLoginI);
        facebookI = findViewById(R.id.facebookLoginI);
        emailT = findViewById(R.id.emailLoginT);
        passwordT = findViewById(R.id.passwordLoginT);
        loginB = findViewById(R.id.loginB);
        forgotPassT = findViewById(R.id.forgotPassT);

        final ConstraintLayout c = findViewById(R.id.loginLayout);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(c);
            }
        });

        signupT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        //Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginGoogle();
            }
        });

        //Facebook Login
        callbackManager = CallbackManager.Factory.create();

        facebookI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "onClick: FB LOGIN START");
                loginFb();

            }
        });

        //User Login
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(loginB);
                loginUser();
            }
        });

        emailT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().isEmpty()){
                    emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box ));
                }
                else {
                    emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box_selected ));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passwordT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()){
                    passwordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box ));
                }
                else{
                    passwordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box_selected ));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        forgotPassT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestNewPass();

            }
        });

    }

    private void requestNewPass() {

        String email = emailT.getText().toString().trim();

        if(email.isEmpty()) {
            emailT.requestFocus();
            emailT.setError("This field can not be empty", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));

        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailT.requestFocus();
            emailT.setError("Please provide a valid email", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
        }
        else {

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
//                        Toast.makeText(getApplicationContext(), "Change password link sent to your mail", Toast.LENGTH_SHORT).show();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Change password link sent to your mail.", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                        snackbar.show();
                    }
                    else {
//                        Toast.makeText(getApplicationContext(), "Please cross check your email again", Toast.LENGTH_SHORT).show();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Please cross check your email again.", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                        snackbar.show();
                    }
                }
            });

        }

    }

    private void loginUser() {

        String email = emailT.getText().toString().trim();
        String password = passwordT.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            emailT.requestFocus();
            emailT.setError("This field can not be empty", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
            passwordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));

        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailT.requestFocus();
            emailT.setError("Please provide a valid email", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
        }
        else if(password.length() <7){
            passwordT.requestFocus();
            passwordT.setError("Password must contain more than 6 characters", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            passwordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
        }
        else {
            showAlertDialog();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.i(TAG, "onComplete: " + user.getUid());
                                dialog.dismiss();
                                updateUI();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {

                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Password should have more than 6 characters.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();

                                } catch(FirebaseAuthInvalidCredentialsException e) {

                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Invalid credentials, please try again.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();

                                } catch(FirebaseAuthUserCollisionException e) {

                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Another user already exist with same credentials.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();

                                }
                                catch(FirebaseAuthEmailException e){
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Please check your email again.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();
                                }
                                catch (FirebaseNetworkException e){
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Oops! Please connect to the internet.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();
                                }

                                catch (FirebaseAuthInvalidUserException e) {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "This user does not exist.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();
                                }
                                catch(Exception e) {
                                    Log.e(TAG, e.getMessage());
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                                    View snackBarView = snackbar.getView();
                                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                    snackbar.show();
                                }
//                                Toast.makeText(LoginActivity.this, "Email or Password not found", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }
    }

    public void showAlertDialog()
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);

        // set the custom layout
        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.alter_loading,
                        null);
        builder.setView(customLayout);

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void hideKeyboard(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void loginGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void loginFb() {
        Log.i(TAG, "loginFb: Starts");
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                        Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                        snackbar.show();

                    }
                });

    }

    private void firebaseAuthWithGoogle(String idToken) {
        showAlertDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.i(TAG, "User ID : "+user.getUid());
                            Log.i(TAG, "Email : "+user.getEmail());
                            Log.i(TAG, "Name : "+user.getDisplayName());
                            Log.i(TAG, "URL : "+user.getPhotoUrl());
                            dialog.dismiss();
                            updateUI();
                        }
                        else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Password should have more than 6 characters.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Invalid credentials, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthUserCollisionException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Another user already exist with same credentials.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            }
                            catch(FirebaseAuthEmailException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Please check your email again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch (FirebaseNetworkException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Oops! Please connect to the internet.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }

                            catch (FirebaseAuthInvalidUserException e) {
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "This user does not exist.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
//                            Toast.makeText(LoginActivity.this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                        }

                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        showAlertDialog();
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.i(TAG, "Email : "+user.getEmail());
                            Log.i(TAG, "Name : "+user.getDisplayName());
                            Log.i(TAG, "URL : "+user.getPhotoUrl());

                            dialog.dismiss();
                            updateUI();
                        }
                        else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Password should have more than 6 characters.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Invalid credentials, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthUserCollisionException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Another user already exist with same credentials.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            }
                            catch(FirebaseAuthEmailException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Please check your email again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch (FirebaseNetworkException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Oops! Please connect to the internet.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }

                            catch (FirebaseAuthInvalidUserException e) {
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "This user does not exist.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
//                            Toast.makeText(LoginActivity.this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                if(e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_REQUIRED){
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Sign in required.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();
                }
                if(e.getStatusCode() == GoogleSignInStatusCodes.NETWORK_ERROR){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Network error, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                if(e.getStatusCode() == GoogleSignInStatusCodes.INVALID_ACCOUNT){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Invalid account, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                if(e.getStatusCode() == GoogleSignInStatusCodes.INTERNAL_ERROR){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                if(e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_FAILED){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout), "Sign in failed, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void updateUI(){

        Intent intent = new Intent(getApplicationContext(), SyncopyActivity.class);
        startActivity(intent);
        finishAffinity();

    }

}