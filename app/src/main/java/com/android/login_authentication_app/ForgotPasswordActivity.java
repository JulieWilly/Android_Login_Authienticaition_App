package com.android.login_authentication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button btnPasswordReset;
    private EditText edtPasswordResetEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_actvitity);

        getSupportActionBar().setTitle("Reset Password.");

        btnPasswordReset = findViewById(R.id.button_password_reset);
        progressBar = findViewById(R.id.resetProgressBar);
        edtPasswordResetEmail = findViewById(R.id.edt_password_reset_email);


        // reset password
        btnPasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtPasswordResetEmail.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your registered email address.", Toast.LENGTH_LONG).show();
                    edtPasswordResetEmail.setError("Email is required.");
                    edtPasswordResetEmail.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your registered email address.", Toast.LENGTH_LONG).show();
                    edtPasswordResetEmail.setError("Valid email is required.");
                    edtPasswordResetEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        // this methods used to send the code and return user back to login
        authProfile = FirebaseAuth.getInstance();

        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please check your email inbox for a pass resent link sent to you.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordActivity.this, "User does not exists or is no longer valid. Please register again!", Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
                
            }
        });
    }
}