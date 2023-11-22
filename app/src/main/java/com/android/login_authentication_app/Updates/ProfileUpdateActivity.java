package com.android.login_authentication_app.Updates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.login_authentication_app.R;
import com.android.login_authentication_app.ReadWriteUserDetails;
import com.android.login_authentication_app.RegisterActivity;
import com.android.login_authentication_app.UploadUserProfileActivity;
import com.android.login_authentication_app.UserProfileActivity;
import com.android.login_authentication_app.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileUpdateActivity extends AppCompatActivity {

    private EditText edtUpdateName, edtUpdateDob, edtUpdatePhone;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton updateGendeSelected;
    private String txtFullName, txtDob,txtGender, txtPhone;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        getSupportActionBar().setTitle("Update Profile Details");

        progressBar = findViewById(R.id.progressBar);
        edtUpdateName = findViewById(R.id.edt_update_profile_name);
        edtUpdateDob = findViewById(R.id.edt_update_profile_Dob);
        edtUpdatePhone = findViewById(R.id.edt_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();


        // method to query and show profile data on the activity.
        showProfile(firebaseUser);

        // upload profile picture method
        Button btnUploadDpPic = findViewById(R.id.upload_dp_pic_btn);
        btnUploadDpPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileUpdateActivity.this, UploadUserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // update email btn
        Button btnUpdateEmail = findViewById(R.id.update_email_btn);
        /*btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileUpdateActivity.this, UploadUserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        }); */

        // set the date picker
        edtUpdateDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // aim:
                // extracting saved dd, mm, yyyy into different variables by creating an array delimited by "/"
                String textSADoB[] = txtDob.split("/");

                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1; // to take care for the month index 0
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                // date picker dialog
                picker = new DatePickerDialog(ProfileUpdateActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        edtUpdateDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        // update profile button
        Button updateProfile = findViewById(R.id.upload_profile_btn);
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
            }
        });
    }

    // update profile
    private void updateProfile(FirebaseUser firebaseUser) {

        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioGroupUpdateGender = findViewById(selectedGenderID);

        // validate users data.
        String mobileRegex = "[6-9][0-9]{9}"; // first number can 6 through 9. the rest 9 nums can be any no.
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(txtPhone);

        // check and confirm user input entries.
        if (TextUtils.isEmpty(txtFullName)) {
            Toast.makeText(ProfileUpdateActivity.this, "Please enter your full name.", Toast.LENGTH_LONG).show();
            // show error around the button
            edtUpdateName.setError("Full name is required.");
            edtUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(txtDob)) {
            Toast.makeText(ProfileUpdateActivity.this, "Please enter your date of birth.", Toast.LENGTH_LONG).show();
            edtUpdateDob.setError("Date of birth required.");
            edtUpdateDob.requestFocus();
        } else if (TextUtils.isEmpty(updateGendeSelected.getText())) {
            Toast.makeText(ProfileUpdateActivity.this, "Please select your gender.", Toast.LENGTH_LONG).show();
            updateGendeSelected.setError("Gender is required.");
            updateGendeSelected.requestFocus();
        } else if (TextUtils.isEmpty(txtPhone)) {
            Toast.makeText(ProfileUpdateActivity.this, "Please enter your mobile no.", Toast.LENGTH_LONG).show();
            edtUpdatePhone.setError("Phone number required.");
            edtUpdatePhone.requestFocus();
        } else if (txtPhone.length() != 10) {
            Toast.makeText(ProfileUpdateActivity.this, "Please re-enter your mobile number.", Toast.LENGTH_LONG).show();
            edtUpdatePhone.setError("Mobile no should be 10 digits");
            edtUpdatePhone.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(ProfileUpdateActivity.this, "Please re-enter your mobile number.", Toast.LENGTH_LONG).show();
            edtUpdatePhone.setError("Mobile no is not valid. Enter strictly 10 digits");
            edtUpdatePhone.requestFocus();
        } else {
//            obtain the data entered by user
            txtGender = updateGendeSelected.getText().toString();
            txtFullName = edtUpdateName.getText().toString();
            txtDob = edtUpdateDob.getText().toString();
            txtPhone = edtUpdatePhone.getText().toString();

            // enter the user data into the firebase realtime database. set up dependencies.
            ReadWriteUserDetails userDetails = new ReadWriteUserDetails(txtDob, txtGender,txtPhone);

            // extract user reference from database for "registered users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");


            // get user registered id
            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // setting new display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(txtFullName).build();

                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(ProfileUpdateActivity.this, "Update Successful!", Toast.LENGTH_LONG).show();

                        // stop the user from returning to updateProfileActivity on pressing back button by closing this current acticity using flags
                        Intent intent = new Intent(ProfileUpdateActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // handle exceptions
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(ProfileUpdateActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    // fetch data from the database.
    private void showProfile(FirebaseUser firebaseUser) {
        // store the id of the current user in the string varaible
        String userRegisteredId = firebaseUser.getUid();

        // extracting  user reference from database for registered users.
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);
        // referencing to the child id that is store to the parent root name of the database.
        referenceProfile.child(userRegisteredId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // access values from the helper class readwritedata.
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    txtFullName = firebaseUser.getDisplayName();
                    txtDob = readUserDetails.doB;
                    txtGender = readUserDetails.gender;
                    txtPhone = readUserDetails.phone;

                    edtUpdateName.setText(txtFullName);
                    edtUpdateDob.setText(txtDob);
                    edtUpdatePhone.setText(txtPhone);

                    // show gender through radio button
                    if (txtGender.equals("Male")) {
                        updateGendeSelected = findViewById(R.id.radio_update_male);
                    } else {
                        updateGendeSelected = findViewById(R.id.radio_update_female);
                    }
                    updateGendeSelected.setChecked(true);
                } else {
                    Toast.makeText(ProfileUpdateActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileUpdateActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(ProfileUpdateActivity.this, ProfileUpdateActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_update_email) {
            Intent intent = new Intent(ProfileUpdateActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_update_settings) {
            Toast.makeText(this, "You have updated your settings.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(ProfileUpdateActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_update_mobile) {
            Intent intent = new Intent(UploadUserProfileActivity.this, MobileUpdateActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UploadUserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(ProfileUpdateActivity.this, "You have signed out.", Toast.LENGTH_SHORT).show();

            //clear the stack to prevent users from coming back to the profile on pressing the back buttong after logging out.
            Intent intent = new Intent(ProfileUpdateActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish(); // close the user activity
        } else {
            // when none of teh menu item was clicked.
            Toast.makeText(ProfileUpdateActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

}