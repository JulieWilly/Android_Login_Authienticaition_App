package com.android.login_authentication_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtDob, edtPhoneNumber, edtPassword, edtConfirmPassword;
    private ProgressBar progressBar;
    private RadioGroup radioGroupGender;
    private RadioButton genderSelected;
    private DatePickerDialog picker;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // set the application title
        getSupportActionBar().setTitle("Register");

        Toast.makeText(this, "You can register now", Toast.LENGTH_SHORT).show();

        edtFullName = findViewById(R.id.edt_register_full_name);
        edtEmail = findViewById(R.id.edt_register_email);
        edtDob = findViewById(R.id.edt_register_Dob);
        edtPhoneNumber = findViewById(R.id.edt_register_mobile);
        edtPassword = findViewById(R.id.edt_register_password);
        edtConfirmPassword = findViewById(R.id.edt_register_confirm_password);

        progressBar = findViewById(R.id.progressBar);
        radioGroupGender = findViewById(R.id.rd_group_gender);
        radioGroupGender.clearCheck();

        // setting date picker for the days of birth
        edtDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get calendar using the current time zone.
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        edtDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        // since the button will not be used globally, we initialize it locally only where it will be used.
        Button registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the checked radio group element
                int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
                genderSelected = findViewById(selectedGenderId);

                // obtaining entered data.
                String txtFullName = edtFullName.getText().toString();
                String txtEmail = edtEmail.getText().toString();
                String txtDob = edtDob.getText().toString();
                String txtPhoneNumber = edtPhoneNumber.getText().toString();
                String txtPwd = edtPassword.getText().toString();
                String txtConfirmPwd = edtConfirmPassword.getText().toString();
                String rdGender; // cannot obtain radio button value before any button has been checked.


                // validate users data.
                String mobileRegex = "[6-9][0-9]{9}"; // first number can 6 through 9. the rest 9 nums can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(txtPhoneNumber);

                // check and confirm user input entries.
                if (TextUtils.isEmpty(txtFullName)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your full name.", Toast.LENGTH_LONG).show();
                    // show error around the button
                    edtFullName.setError("Full name is required.");
                    edtFullName.requestFocus();
                } else if (TextUtils.isEmpty(txtEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    edtEmail.setError("Email required.");
                } else if (TextUtils.isEmpty(txtDob)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your date of birth.", Toast.LENGTH_LONG).show();
                    edtDob.setError("Date of birth required.");
                    edtDob.requestFocus();
                } else if (radioGroupGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender.", Toast.LENGTH_LONG).show();
                    genderSelected.setError("Gender is required.");
                    genderSelected.requestFocus();
                } else if (TextUtils.isEmpty(txtPhoneNumber)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your mobile no.", Toast.LENGTH_LONG).show();
                    edtPhoneNumber.setError("Phone number required.");
                    edtPhoneNumber.requestFocus();
                } else if (txtPhoneNumber.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your mobile number.", Toast.LENGTH_LONG).show();
                    edtPhoneNumber.setError("Mobile no should be 10 digits");
                    edtPhoneNumber.requestFocus();
                } else if (mobileMatcher.find()) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your mobile number.", Toast.LENGTH_LONG).show();
                    edtPhoneNumber.setError("Mobile no is not valid. Enter strictly 10 digits");
                    edtPhoneNumber.requestFocus();
                } else if (TextUtils.isEmpty(txtPwd)) {
                    Toast.makeText(RegisterActivity.this, "Enter your password.", Toast.LENGTH_LONG).show();
                    edtPassword.setError("Password required.");
                    edtPassword.requestFocus();
                } else if (txtPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least six(6) digits", Toast.LENGTH_LONG).show();
                    edtPassword.setError("Password too weak.");
                    edtPassword.requestFocus();
                } else if (TextUtils.isEmpty(txtConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please confirm your password.", Toast.LENGTH_LONG).show();
                    edtConfirmPassword.setError("Password confirmation required.");
                    edtConfirmPassword.requestFocus();
                } else if (!txtPwd.equals(txtConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please enter matching password to confirm.", Toast.LENGTH_LONG).show();
                    edtConfirmPassword.setError("Password confirmation required.");
                    edtConfirmPassword.requestFocus();

                    // clear the entered passwords.
                    edtPassword.clearComposingText();
                    edtConfirmPassword.clearComposingText();
                } else {
                    rdGender = genderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);

                    registerUser(txtFullName, txtEmail, rdGender, txtDob, txtPhoneNumber, txtPwd, txtConfirmPwd);
                }
            }

        });

    }

    // register user using the details provided.
    private void registerUser(String txtFullName, String txtEmail, String rdGender, String txtDob, String txtPhoneNumber, String txtPwd, String txtConfirmPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(txtEmail, txtPwd).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
            // creating the user profile
            @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // send email to user so that used can authenticate that he or she
                            // is the one doing the application or rather registration.
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            // update display name of the user.
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(txtFullName).build();
                            firebaseUser.updateProfile(profileChangeRequest);
                            // Enter user data into the Firebase realtime database.
                            ReadWriteUserDetails readWriteUserDetails = new ReadWriteUserDetails(txtDob, rdGender, txtPhoneNumber);

                            //extracting user reference from database for "Registered Users"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                            referenceProfile.child(firebaseUser.getUid()).setValue(readWriteUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // send email
                                        firebaseUser.sendEmailVerification();

                                        Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email", Toast.LENGTH_LONG).show();

                                        // open user profile after successful registration.
                                        Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                        // limit the user from going back to the Registering activity on pressing the back button after making registration.
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "User registration failed.", Toast.LENGTH_LONG).show();
                                    }
                                    // hide the progress bar despite failure or success of registration.
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            // enter an exception if user enter less details.
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                edtPassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and capital letters.");
                                edtPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                edtPassword.setError("Your email is invalid or already in use. Please re-enter your email.");
                                edtPassword.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                edtPassword.setError("User is already registered with this email. Use another email to register.");
                                edtPassword.requestFocus();
                            } catch (Exception e) {
                                // TAG - identifies the class or activity where log call occurs.
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
}