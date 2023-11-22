package com.android.login_authentication_app.Updates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class ChangePasswordActivity extends AppCompatActivity {

    FirebaseAuth authProfile;
    private EditText edtCurrPwd, edtNewPwd, edtConfirmNewPwd;
    private TextView txtCurrAuthenticatedPwd;
    private Button btnChangePwd, btnAuthenticateCurrPwd;
    private ProgressBar progressBar;
    private String userPwdCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // set title for the activity
        getSupportActionBar().setTitle("Title Password.");

        edtCurrPwd = findViewById(R.id.edt_verify_curr_pwd);
        edtNewPwd = findViewById(R.id.edt_new_pwd);
        edtConfirmNewPwd = findViewById(R.id.edt_confirm_new_pwd);
        txtCurrAuthenticatedPwd = findViewById(R.id.txt_curr_pwd_authenticated);
        progressBar = findViewById(R.id.progressBar_chg);
        btnAuthenticateCurrPwd = findViewById(R.id.btn_authenticate_pwd);
        btnChangePwd = findViewById(R.id.btn_update_new_pwd);
        
        // disable buttons.
        edtNewPwd.setEnabled(false);
        edtConfirmNewPwd.setEnabled(false);
        btnChangePwd.setEnabled(false);
        
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        
        // check if firebase will return null or the user.
        if (firebaseUser.equals("")) {
            Toast.makeText(this, "Something went wrong! Users details not available.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
        




    }

    // first re-authenticate user before changing the password.
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        btnAuthenticateCurrPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwdCurr = edtCurrPwd.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)) {
                    Toast.makeText(ChangePasswordActivity.this, "Password is needed!", Toast.LENGTH_LONG).show();
                    edtCurrPwd.setError("Please enter your current password to authenticate.");
                    edtCurrPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    // Re-authenticate user at this level.
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // first remove the visibility of the progress bar.
                                progressBar.setVisibility(View.GONE);

                                // disable
                                edtCurrPwd.setEnabled(false);
                                edtConfirmNewPwd.setEnabled(true);
                                edtNewPwd.setEnabled(true);

                                //enable change pwd btn. Disable the authenticate button
                                btnChangePwd.setEnabled(true);
                                btnAuthenticateCurrPwd.setEnabled(false);

                                // set TestView to notify user us authenticated/verified to proceed change the password.
                                txtCurrAuthenticatedPwd.setText("You are authenticated/verified." + " You can change password now! ");

                                // Update the color of the change pwd button
                                btnChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePasswordActivity.this,R.color.dark_green));

                                btnChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // actual method for changing the pwd.
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        /**
         * BEFORE CHANGING THE PASSWORD, WE HAVE TO PERFORM THE FOLLOWING CHECKS FIRST:
         * if the user has entered a pwd
         * if teh user has confirmed the password.
         * if the new password and the confirmed password matches 
         * if the old password is the same as new password.
         */
        String userNewPwd = edtNewPwd.getText().toString();
        String userConfirmNewPwd = edtConfirmNewPwd.getText().toString();
        
        if (TextUtils.isEmpty(userNewPwd)) {
            Toast.makeText(this, "New password is needed.", Toast.LENGTH_SHORT).show();
            edtNewPwd.setError("Please enter your new password.");
            edtNewPwd.requestFocus();
        } else if (TextUtils.isEmpty(userConfirmNewPwd)) {
            Toast.makeText(this, "Please confirm your new password!", Toast.LENGTH_SHORT).show();
            edtConfirmNewPwd.setError("Please re-enter your new password!");
            edtConfirmNewPwd.requestFocus();
        } else if (!userNewPwd.matches(userConfirmNewPwd)) {
            Toast.makeText(this, "Password did not match!", Toast.LENGTH_SHORT).show();
            edtConfirmNewPwd.setError("Please re-enter the same password!");
            edtConfirmNewPwd.requestFocus();
        } else if (userPwdCurr.matches(userNewPwd)) {
            Toast.makeText(this, "New password cannot be the same as the old password!", Toast.LENGTH_SHORT).show();
            edtNewPwd.setError("Please enter new password!");
            edtNewPwd.requestFocus();
        } else {
            // make the progress bar visible once more.
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userNewPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
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
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.menu_update_email) {
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_settings) {
            Toast.makeText(this, "You have updated your settings.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_update_mobile) {
            Intent intent = new Intent(ChangePasswordActivity.this, MobileUpdateActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_delete_profile) {
            Intent intent = new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(ChangePasswordActivity.this, "You have signed out.", Toast.LENGTH_SHORT).show();

            //clear the stack to prevent users from coming back to the profile on pressing the back button after logging out.
            Intent intent = new Intent(ChangePasswordActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish(); // close the user activity
        } else {
            // when none of teh menu item was clicked.
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


}