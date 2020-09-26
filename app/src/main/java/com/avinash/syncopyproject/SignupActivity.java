package com.avinash.syncopyproject;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SignupActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String EMAIL = "email";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ImageView googleI;
    private ImageView facebookI;
    private CallbackManager callbackManager;
    private static final String TAG = "SignupActivity";
    private Button signInB;
    private TextView emailT;
    private TextView passwordT;
    private TextView rePasswordT;
    private TextView loginT;
    private ConstraintLayout signupLayout;
    private AlertDialog dialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupLayout = findViewById(R.id.signupLayout);
        loginT = findViewById(R.id.loginT);
        googleI = findViewById(R.id.googleSignupI);
        facebookI = findViewById(R.id.facebookSignupI);
        signInB = findViewById(R.id.signupB);
        emailT = findViewById(R.id.emailSignupT);
        passwordT = findViewById(R.id.passwordSignupT);
        rePasswordT = findViewById(R.id.passwordRetypeSignupT);

        signupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(signupLayout);
            }
        });

        //Google Login
        mAuth = FirebaseAuth.getInstance();

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

        //User Sign In
        signInB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(signInB);
                signInUser();

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
        rePasswordT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty())
                    rePasswordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box ));
                else
                    rePasswordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.text_box_selected ));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        loginT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

            }
        });
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

    private void signInUser() {

        String email = emailT.getText().toString().trim();
        String password = passwordT.getText().toString().trim();
        String rePassword = rePasswordT.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            emailT.requestFocus();
            emailT.setError("This field can not be empty", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            emailT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
            passwordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
            rePasswordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
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

        else if(!password.equals(rePassword)) {
            rePasswordT.requestFocus();
            rePasswordT.setError("Password does not match", ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_name));
            rePasswordT.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.error_text_box ));
        }
        else{
        showAlertDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.i(TAG, "User : "+user.getUid());
                            dialog.dismiss();
                            updateUI("", user.getUid());
                        }
                        else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Password should have more than 6 characters.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Invalid credentials, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthUserCollisionException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Another user already exist with same credentials.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            }
                            catch(FirebaseAuthEmailException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Please check your email again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch (FirebaseNetworkException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Oops! Please connect to the internet.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }

                            catch (FirebaseAuthInvalidUserException e) {
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "This user does not exist.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            dialog.dismiss();
                        }

                    }
                });
        }
    }

    private void loginGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void loginFb() {
        Log.i(TAG, "loginFb: Starts");
        LoginManager.getInstance().logInWithReadPermissions(SignupActivity.this, Arrays.asList("email", "public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();


                    }
                });

    }

    private void hideKeyboard(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

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
                            updateUI(user.getDisplayName(), user.getUid());
                        }
                        else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Password should have more than 6 characters.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Invalid credentials, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthUserCollisionException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Another user already exist with same credentials.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            }
                            catch(FirebaseAuthEmailException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Please check your email again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch (FirebaseNetworkException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Oops! Please connect to the internet.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }

                            catch (FirebaseAuthInvalidUserException e) {
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "This user does not exist.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
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
                            dialog.dismiss();
                            updateUI(user.getDisplayName(), user.getUid());
                        }
                        else {

                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Password should have more than 6 characters.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Invalid credentials, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            } catch(FirebaseAuthUserCollisionException e) {

                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Another user already exist with same credentials.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();

                            }
                            catch(FirebaseAuthEmailException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Please check your email again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch (FirebaseNetworkException e){
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Oops! Please connect to the internet.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }

                            catch (FirebaseAuthInvalidUserException e) {
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "This user does not exist.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }
                            catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                                snackbar.show();
                            }

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
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
                Log.w(TAG, "Google sign in failed", e);
                if(e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_REQUIRED){
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Sign in required.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();
                }
                 if(e.getStatusCode() == GoogleSignInStatusCodes.NETWORK_ERROR){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Network error, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                 if(e.getStatusCode() == GoogleSignInStatusCodes.INVALID_ACCOUNT){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Invalid account, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                 if(e.getStatusCode() == GoogleSignInStatusCodes.INTERNAL_ERROR){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Something went wrong, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
                if(e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_FAILED){

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.signupLayout), "Sign in failed, please try again.", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.hint));
                    snackbar.show();

                }
//                Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void updateUI(String name, String userid) {
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        intent.putExtra(EditProfileActivity.USERNAME, name);
        intent.putExtra(EditProfileActivity.USERID, userid);
        startActivity(intent);
    }

}
