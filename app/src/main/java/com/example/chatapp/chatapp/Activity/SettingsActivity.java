package com.example.chatapp.chatapp.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{


    private CircleImageView settingPorileImage;
    private TextView settingsDisplayName;
    private TextView settingsStatus;
    private Button settingsImageButton;
    private Button settingsStatusButton;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private Uri profileImage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpSettings();
        userData();
        settingsImageButton.setOnClickListener(this);
        settingsStatusButton.setOnClickListener(this);
    }


    public void setUpSettings(){
        settingPorileImage = (CircleImageView) findViewById(R.id.settingPorileImage);
        settingsDisplayName = (TextView) findViewById(R.id.settingsDisplayName);
        settingsStatus = (TextView) findViewById(R.id.settingsStatus);
        settingsImageButton = (Button) findViewById(R.id.settingsImageButton);
        settingsStatusButton = (Button) findViewById(R.id.settingsStatusButton);
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void userData(){
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("image").getValue().toString().equals("default")) {
                    Picasso.with(SettingsActivity.this).load(dataSnapshot.child("image").getValue().toString())
                            .placeholder(R.drawable.placeholder)
                            .into(settingPorileImage);
                }
                settingsDisplayName.setText(dataSnapshot.child("name").getValue().toString());
                settingsStatus.setText(dataSnapshot.child("status").getValue().toString());
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("DB-Error",databaseError.getMessage());
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.settingsImageButton:
                userProfileImage();
                break;
            case R.id.settingsStatusButton:
                changeStatus();
                break;
        }

    }

    public void changeStatus(){
        Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
        intent.putExtra("Data",settingsStatus.getText().toString());
        startActivity(intent);
    }

    public void userProfileImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("My Crop")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("Done")
                .setRequestedSize(400, 400)
                .setCropMenuCropButtonIcon(R.drawable.donewhite).start(this);

    }

    public void uploadImage(){
        if(profileImage != null){
            final StorageReference filepath = storageReference.child("User_profile_images").child(profileImage.getLastPathSegment());
            filepath.putFile(profileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            databaseReference.child(user.getUid()).child("image").setValue(uri.toString());
                            progressDialog.dismiss();
                        }

                    }); progressDialog.dismiss();
                }});
        }else {
            Toast.makeText(this, "Image == null", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Uploading image...");
                //progressDialog.setMessage("");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                profileImage = result.getUri();
                settingPorileImage.setImageURI(profileImage);
                uploadImage();
                Toast.makeText(
                        this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG)
                        .show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
