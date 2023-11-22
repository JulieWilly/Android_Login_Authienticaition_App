package com.android.login_authentication_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.login_authentication_app.Updates.ChangePasswordActivity;
import com.android.login_authentication_app.Updates.ProfileUpdateActivity;
import com.android.login_authentication_app.Updates.UpdateEmailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private TextView txtViewWelcome, txtViewEmail, txtViewFullName,txtViewDob, txtViewGender, txtViewMobile;
    private ProgressBar progressBar;
    private String fullName, email, doB, gender, mobile;
    private ImageView img;
    private FirebaseAuth authProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("Home/User details.");

        txtViewWelcome = findViewById(R.id.txt_show_welcome);
        txtViewEmail = findViewById(R.id.txt_show_email);
        txtViewFullName = findViewById(R.id.txt_show_full_name);
        txtViewDob = findViewById(R.id.txt_show_Dob);
        txtViewGender = findViewById(R.id.txt_show_gender);
        txtViewMobile = findViewById(R.id.txt_show_phone);

        progressBar = findViewById(R.id.user_Profile_progressBar);

        // creating an onclick
        img = findViewById(R.id.img_profile);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, UploadUserProfileActivity.class);
                startActivity(intent);
            }
        });
        // call for an instance of the firebase,
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // check if the user is present.
        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong! User's details are not available. Please register again.", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
    }


    //users comming to usersprofile acitivity after successful login.
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        // set the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email Not Verified.");
        builder.setMessage("Please verify your email now. You can not login without email verification the next time.");

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

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        // extracting references from the database for registered users.
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                // check if the return is null
                if (readWriteUserDetails != null) {
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readWriteUserDetails.doB;
                    gender = readWriteUserDetails.gender;
                    mobile = readWriteUserDetails.phone;

                    // set welcome text

                    txtViewWelcome.setText("Welcome ," + fullName + "!");
                    txtViewEmail.setText(email);
                    txtViewFullName.setText(fullName);
                    txtViewDob.setText(doB);
                    txtViewGender.setText(gender);
                    txtViewMobile.setText(mobile);

                    // Set user Dp(After user has uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    // ImageViewer setImageUri()
                    // should not be used with regular URIS, so we are using picasso
                    Picasso.with(UserProfileActivity.this).load(uri).into(img);
                } else {
                    Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "You can now log in", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(UserProfileActivity.this, ProfileUpdateActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.menu_update_email) {
            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_update_settings) {
            Toast.makeText(this, "You have updated your settings.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_update_mobile) {
            Intent intent = new Intent(UserProfileActivity.this, MobileUpdateActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(this, "You have signed out.", Toast.LENGTH_SHORT).show();

            //clear the stack to prevent users from coming back to the profile on pressing the back button after logging out.
            Intent intent = new Intent(UserProfileActivity.this, WelcomeActivity.class);
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