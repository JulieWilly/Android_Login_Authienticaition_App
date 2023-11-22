package com.android.login_authentication_app.Updates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.login_authentication_app.R;
import com.android.login_authentication_app.UserProfileActivity;
import com.android.login_authentication_app.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private TextView txtViewAuthenticated;
    private String userOldEmail, userNewEmail, userPwd;
    private Button btnUpdateEmail;
    private EditText edtNewEmail, edtPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        // set activity title
        getSupportActionBar().setTitle("Update Email");

        progressBar = findViewById(R.id.update_email_progressBar);
        txtViewAuthenticated = findViewById(R.id.txt_curr_pwd_authenticated);
        btnUpdateEmail = findViewById(R.id.btn_update_new_pwd);
        edtNewEmail = findViewById(R.id.edt_new_pwd);
        edtPwd = findViewById(R.id.edt_verify_curr_pwd);


        // make button disable until the user is authenticated.
        btnUpdateEmail.setEnabled(false);
        edtNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // set old email ID on the EmailTextView
        userNewEmail = firebaseUser.getEmail();
        TextView txtOldEmail = findViewById(R.id.txt_update_email_old);
        txtOldEmail.setText(userOldEmail);

        if (firebaseUser.equals("")) {
            // if the user is null, no email has been retrieved.
            Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_LONG).show();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    //verify user before updating the email
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        Button btnVerifyUser = findViewById(R.id.btn_authenticate_pwd);
        btnVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // obtaining password for authentication
                userPwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue.", Toast.LENGTH_LONG).show();
                    edtPwd.setError("Please enter your password for authentication");
                    edtPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    // authenticate credentials
                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //remove progressBar
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UpdateEmailActivity.this," Password has been verified." + "You can update your email now", Toast.LENGTH_SHORT).show();

                                // set textview to show that user is authenticated.
                                txtViewAuthenticated.setText("You are authenticated. You can update your email now");

                                // Disable EditText for password, button to veify user and enable
                                // EditText for new email and update email
                                edtNewEmail.setEnabled(true);
                                edtPwd.setEnabled(false);
                                btnVerifyUser.setEnabled(true);
                                btnUpdateEmail.setEnabled(true);

                                // change the color of the update button
                                btnUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this, R.color.dark_green));

                                btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail = edtNewEmail.getText().toString();

                                        if(TextUtils.isEmpty(userNewEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "New email is required", Toast.LENGTH_SHORT).show();
                                            edtNewEmail.setError("Please enter new email");
                                            edtNewEmail.requestFocus();
                                        } else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter a valid Email", Toast.LENGTH_SHORT).show();
                                            edtNewEmail.setError("Please provide valid Email.");
                                            edtNewEmail.requestFocus();
                                        } else if (userOldEmail.matches(userNewEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "New email cannot be same as the old Email.", Toast.LENGTH_SHORT).show();
                                            edtNewEmail.setError("Please enter new Email");
                                            edtNewEmail.requestFocus();
                                        } else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                }
                            }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // verity email with the existing email in the data base.
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated. Please verify", Toast.LENGTH_SHORT).show();

                    // move to user activity
                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // method associated with android menus
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu items
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // called when any menu item is selected.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresher) {
            // refresh the activity
            startActivity(getIntent());
            finish();

            // remove the transition occurring while doing a refresh
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this, ProfileUpdateActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateEmailActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_settings) {
            Toast.makeText(this, "You have updated your settings.", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UpdateEmailActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_update_mobile) {
            Intent intent = new Intent(UploadUserProfileActivity.this, MobileUpdateActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UploadUserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateEmailActivity.this, "You have signed out.", Toast.LENGTH_SHORT).show();

            //clear the stack to prevent users from coming back to the profile on pressing the back buttong after logging out.
            Intent intent = new Intent(UpdateEmailActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish(); // close the user activity
        } else {
            // when none of teh menu item was clicked.
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

}