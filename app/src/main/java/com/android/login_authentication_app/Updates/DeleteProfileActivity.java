package com.android.login_authentication_app.Updates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
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

public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText edtUserPwd;
    private TextView txtUserAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button btnUserAuthenticated, btnUserDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        // set title
        getSupportActionBar().setTitle("Delete your profile");

        progressBar = findViewById(R.id.progressBar_delete_profile);
        txtUserAuthenticated = findViewById(R.id.txt_delete_profile_authenticated);
        edtUserPwd = findViewById(R.id.edt_delete_profile_pwd);
        btnUserAuthenticated = findViewById(R.id.btn_authenticate_delete_profile_pwd);
        btnUserDeleted = findViewById(R.id.btn_delete_profile);

        // disable some buttons
        btnUserDeleted.setEnabled(false);

        // get firebase instance
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // Check if it will return empty details
        if (firebaseUser.equals("")) {
            Toast.makeText(this, "Something went wrong!" + " User details are not available at the moment.", Toast.LENGTH_SHORT).show();
            // send the user to the user or home activity.

            Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            // if the details are present, proceed to authenticate users password entry

            reAuthenticateUser(firebaseUser);
        }
    }

    // first re-authenticate user before changing the password.
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        btnUserAuthenticated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd = edtUserPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(DeleteProfileActivity.this, "Password is needed!", Toast.LENGTH_LONG).show();
                    edtUserPwd.setError("Please enter your current password to authenticate.");
                    edtUserPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    // Re-authenticate user at this level.
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // first remove the visibility of the progress bar.
                                progressBar.setVisibility(View.GONE);

                                // disable editText for password
                                edtUserPwd.setEnabled(false);


                                //enable change pwd btn. Disable the authenticate button
                                btnUserDeleted.setEnabled(true);
                                btnUserAuthenticated.setEnabled(false);

                                // set TestView to notify user us authenticated/verified to proceed change the password.
                                txtUserAuthenticated.setText("Your password has been verified. " + " You can now remove your account!");

                                Toast.makeText(DeleteProfileActivity.this, "\"Your password has been verified. \" + \" You can delete your profile now. this action is irreversible!\"", Toast.LENGTH_SHORT).show();
                                // Update the color of the change pwd button
                                btnUserDeleted.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this,R.color.dark_green));

                                btnUserDeleted.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // actual method for changing the pwd.
                                       showAlertDialog();
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        // set the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete user and related data!");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible.");

        // open email apps if user click/taps the button.
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /**
                 * Delete account if you chooses continue*/

                deleteUserProfile(firebaseUser);
            }
        });
        // return the user to the profile activity if the users opts not to delete account
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(DeleteProfileActivity.this, ProfileUpdateActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // create the alertDialog
        AlertDialog alertDialog = builder.create();


        // changing the color of the alert dialog buttons
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });
        // show the alertDialog
        alertDialog.show();
    }

    private void deleteUserProfile(FirebaseUser firebaseUser) {
        // delete data related to the user from the firebase.
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "User has been deleted!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(DeleteProfileActivity.this, ProfileUpdateActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.menu_update_email) {
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_settings) {
            Toast.makeText(this, "You have updated your settings.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_update_mobile) {
            Intent intent = new Intent(UserProfileActivity.this, MobileUpdateActivity.class);
            startActivity(intent);
        } */else if(id == R.id.menu_delete_profile) {
            Intent intent = new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(this, "You have signed out.", Toast.LENGTH_SHORT).show();

            //clear the stack to prevent users from coming back to the profile on pressing the back button after logging out.
            Intent intent = new Intent(DeleteProfileActivity.this, WelcomeActivity.class);
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