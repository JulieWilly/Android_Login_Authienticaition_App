package com.android.login_authentication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPassword, edtEmail;
    private ProgressBar progressBar;
    private FirebaseAuth  authProfile;
    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set title
        getSupportActionBar().setTitle("Login");

        // access widgets.
        edtEmail = findViewById(R.id.edt_login_email);
        edtPassword = findViewById(R.id.edt_login_password);
        progressBar = findViewById(R.id.login_progressBar);

        // direct user to the user profile.
        authProfile = FirebaseAuth.getInstance();

        // forgot password button
        Button btnForgotPassword = findViewById(R.id.button_forgot_password);
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "You cab reset your password now!", Toast.LENGTH_SHORT).show();
            }
        });
        // setting the hide and show password image properties.
        ImageView imgShowHidePwd = findViewById(R.id.img_pass_show_hide);
        // set the image resource.
        imgShowHidePwd.setImageResource(R.drawable.eye_hide_icon);
        imgShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    // if the password is visible then hide it.
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    // change icon
                    imgShowHidePwd.setImageResource(R.drawable.eye_hide_icon);
                } else {
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgShowHidePwd.setImageResource(R.drawable.eye_show_icon);
                }
            }
        });


        // login user.
        Button btnLogin = findViewById(R.id.button_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtEmail = edtEmail.getText().toString();
                String txtPassword = edtPassword.getText().toString();

                if (TextUtils.isEmpty(txtEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter your email.", Toast.LENGTH_LONG).show();
                    edtEmail.setError("Email address required.");
                    edtEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please re-enter your email.", Toast.LENGTH_LONG).show();
                    edtEmail.setError("Valid email address required.");
                    edtEmail.requestFocus();

                } else if (TextUtils.isEmpty(txtPassword)) {
                    Toast.makeText(LoginActivity.this, "Please enter the correct password.", Toast.LENGTH_LONG).show();
                    edtPassword.setError("Password required.");
                    edtPassword.requestFocus();
                } else {
                    // set the progress bar to show user progress.
                    progressBar.setVisibility(View.VISIBLE);
                    LoginUser(txtEmail,txtPassword);
                }
            }
        });
    }

    private void LoginUser(String email, String password) {
    // method to authenticate the user in the firebase.
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // check if the tesk was competed.
                if (task.isSuccessful()) {
                    // get the instance of the current user.
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    // check if user has verified the email.
                    if (firebaseUser.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "You are logged in now.", Toast.LENGTH_SHORT).show();

                        // open the user profile.
                        // start User profile activity
                        startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
                        // finish this activity
                        finish();
                    } else {
                        firebaseUser.sendEmailVerification();
                        // sign out the user/
                        authProfile.signOut();
                        showAlertDialog();
                    }

                } else {
                    // exception handling
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        edtEmail.setError("User does not exist or is no longer valid. Please register again.");
                        edtEmail.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        edtEmail.setError("Invalid credentials. Kindly, check and re-enter details.");
                        edtEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void showAlertDialog() {
        // set the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified.");
        builder.setMessage("Please verify your email now. You can not login without email verification.");

        // open email apps if user click/taps the button.
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// redirects to open email application in a new window.
                startActivity(intent);
            }
        });

        // create the alertDialog
        AlertDialog alertDialog = builder.create();

        // show the alertDialog
        alertDialog.show();
    }


    // check if user is already logged in or not
    // if logged on, take user direct to the user profile.

    @Override
    protected void onStart() {
        super.onStart();

        if (authProfile.getCurrentUser() != null) { // impliea that if not null, the user is logged in already
            Toast.makeText(this, "You are already logged in.", Toast.LENGTH_SHORT).show();

            // start user profile activity

            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            // finish this activity
            finish();
        } else{
            Toast.makeText(this, "You can log in now.", Toast.LENGTH_SHORT).show();
        }
    }
}