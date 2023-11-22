package com.android.login_authentication_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.login_authentication_app.Updates.ChangePasswordActivity;
import com.android.login_authentication_app.Updates.UpdateEmailActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadUserProfileActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imgViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference   storageReference;
    private FirebaseUser firebaseUser;

    private static final int PICK_IMAGE_REQUEST = 1;// implies it is tue
    private Uri uriImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_user_profile);

        // set activity title
        getSupportActionBar().setTitle("Upload profile picture");

        progressBar = findViewById(R.id.user_pic_Upload_progressBar);
        imgViewUploadPic = findViewById(R.id.img_profile_dp);

        Button btnUploadPicChoose = findViewById(R.id.upload_pic_choose_btn);
        Button btnUploadPic = findViewById(R.id.upload_pic_button);
        // get firebase authentication instance.
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");


        // uniform resource identifier
        // used to identify resources
        Uri uri = firebaseUser.getPhotoUrl();
        // set users current DP/profile image in view(if uploaded already
        // we will use the picasso library

        Picasso.with(UploadUserProfileActivity.this).load(uri).into(imgViewUploadPic);

        // choosing image for the dp
        // open file chooser listener
        btnUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        // uploading the image to firebase now
        btnUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show the progress bar.
                progressBar.setVisibility(View.VISIBLE);
                uploadPic();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT); // used to also select images mime type images.
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
            Intent intent = new Intent(UploadUserProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.menu_update_email) {
            Intent intent = new Intent(UploadUserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_settings) {
            Toast.makeText(this, "You have updated your settings.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UploadUserProfileActivity.this, ChangePasswordActivity.class);
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
            Toast.makeText(UploadUserProfileActivity.this, "You have signed out.", Toast.LENGTH_SHORT).show();

            //clear the stack to prevent users from coming back to the profile on pressing the back buttong after logging out.
            Intent intent = new Intent(UploadUserProfileActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish(); // close the user activity
        } else {
            // when none of teh menu item was clicked.
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Line below checks whether pick-oimage-reuest is true and whether there is data collected
        if (requestCode == PICK_IMAGE_REQUEST &&  resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imgViewUploadPic.setImageURI(uriImage);
        }
    }

    // uploading image to the firebase.
    private void uploadPic() {
        if (uriImage != null) {
            // store the image with the uid of teh current logged in user.
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "." + getFileExtension(uriImage));

            // now upload the image into storage.
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // set the display image of the user after uploading
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUrl).build();
                            firebaseUser.updateProfile(profileChangeRequest);
                        }
                    });
                    
                    // stop the progress bar if the upload is successful
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(UploadUserProfileActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UploadUserProfileActivity.this,UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadUserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No profile picture was selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uriImage) {
        // communicate with content provide using content resolver.
        ContentResolver cR = getContentResolver();
        // use a media type(MIME) standard and format of a document over the internet
        MimeTypeMap mime = MimeTypeMap.getSingleton(); // get the extension format of the image.
        return mime.getExtensionFromMimeType(cR.getType(uriImage)); // return the extension to the calling class
    }
}