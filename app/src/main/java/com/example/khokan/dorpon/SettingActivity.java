package com.example.khokan.dorpon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText setting_user_status,setting_user_name,setting_user_full_name, setting_user_country, setting_user_gender, setting_user_date_of_birth, setting_relationship_status;
    private Button account_setting_update_button;
    private CircleImageView setting_profile_image;
    private FirebaseAuth mAuth;
    private DatabaseReference settingRef;
    private String currentUserId;
    private ProgressDialog mDialog;
    private final static int GALLERY_PICK=1;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

//        firebase ref
        mAuth =FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        mToolbar = findViewById(R.id.setting_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        Initialization
        setting_user_status = findViewById(R.id.setting_user_status);
        setting_user_full_name = findViewById(R.id.setting_user_full_name);
        setting_user_country = findViewById(R.id.setting_user_country);
        setting_user_date_of_birth= findViewById(R.id.setting_user_dateOfBirth);
        setting_relationship_status = findViewById(R.id.setting_relationship_status);
        setting_user_gender= findViewById(R.id.setting_user_gender);
        setting_profile_image = findViewById(R.id.setting_profile_image);
        account_setting_update_button =findViewById(R.id.setting_account_update_button);
        setting_user_name =findViewById(R.id.setting_user_name);

        mDialog = new ProgressDialog(this);

        setting_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        settingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("userFullName").getValue().toString();
                    String userName = dataSnapshot.child("userName").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userCountry = dataSnapshot.child("userCountry").getValue().toString();
                    String userGender = dataSnapshot.child("gender").getValue().toString();
                    String userBirth = dataSnapshot.child("dob").getValue().toString();
                    String userRelationship = dataSnapshot.child("relationship").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileImages").getValue().toString();

                    setting_user_status.setText(userStatus);
                    setting_user_full_name.setText(userFullName);
                    setting_user_country.setText(userCountry);
                    setting_user_date_of_birth.setText(userBirth);
                    setting_relationship_status.setText(userRelationship);
                    setting_user_gender.setText(userGender);
                    setting_user_name.setText(userName);
                    Picasso.with(SettingActivity.this).load(userProfileImage).placeholder(R.drawable.profile).into(setting_profile_image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        account_setting_update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateUserName = setting_user_name.getText().toString();
                String updateUserStatus = setting_user_status.getText().toString();
                String updateUserFullName = setting_user_full_name.getText().toString();
                String updateUserGender= setting_user_gender.getText().toString();
                String updateUserRelationship = setting_relationship_status.getText().toString();
                String updateUserCountry = setting_user_country.getText().toString();
                String updateUserBirth = setting_user_date_of_birth.getText().toString();

                if (TextUtils.isEmpty(updateUserName))
                {
                    Toast.makeText(SettingActivity.this, "Please write your user name...", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(updateUserFullName))
                {
                    Toast.makeText(SettingActivity.this, "Please write your full name...", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(updateUserStatus))
                {
                    Toast.makeText(SettingActivity.this, "Please write your status...", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(updateUserRelationship))
                {
                    Toast.makeText(SettingActivity.this, "Please write your Relationship...", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(updateUserGender))
                {
                    Toast.makeText(SettingActivity.this, "Please write your Gender...", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(updateUserCountry))
                {
                    Toast.makeText(SettingActivity.this, "Please write your Country...", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(updateUserBirth))
                {
                    Toast.makeText(SettingActivity.this, "Please write your birth date...", Toast.LENGTH_SHORT).show();
                }else
                {
                    mDialog.setTitle("Updating Account");
                    mDialog.setMessage("Please Wait While we are updating Your Account....");
                    mDialog.setCanceledOnTouchOutside(true);
                    mDialog.show();
                    updateAccountInfo(updateUserName,updateUserStatus,updateUserFullName,updateUserGender,updateUserRelationship,updateUserCountry,updateUserBirth );
                }


            }
        });

    }

    private void updateAccountInfo(String updateUserName, String updateUserStatus, String updateUserFullName, String updateUserGender, String updateUserRelationship, String updateUserCountry, String updateUserBirth) {

        HashMap userSettingMap = new HashMap();
        userSettingMap.put("userName",updateUserName);
        userSettingMap.put("userFullName", updateUserFullName);
        userSettingMap.put("userCountry", updateUserCountry);
        userSettingMap.put("status", updateUserStatus);
        userSettingMap.put("gender",updateUserGender);
        userSettingMap.put("dob",updateUserBirth);
        userSettingMap.put("relationship", updateUserRelationship);
        settingRef.updateChildren(userSettingMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    sendToMainActivity();
                    Toast.makeText(SettingActivity.this, "Account Updated Successfully....", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
                else
                {
                    String error  = task.getException().getMessage().toString();
                    Toast.makeText(SettingActivity.this, "Error Occurred :"+error, Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
            }
        });

    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {

                mDialog.setTitle("Image Uploading...");
                mDialog.setMessage("Please Wait While we are updating Your Profile Image....");
                mDialog.setCanceledOnTouchOutside(true);
                mDialog.show();

                Uri resultUri = result.getUri();
                StorageReference filePath = userProfileImageRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            String imageDownloadUrl=task.getResult().getDownloadUrl().toString();
                            settingRef.child("profileImages").setValue(imageDownloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        mDialog.dismiss();
                                        Intent selfIntent = new Intent(SettingActivity.this, SettingActivity.class);
                                        startActivity(selfIntent);
                                        Toast.makeText(SettingActivity.this, "Image uploaded successfully....", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        mDialog.dismiss();
                                        Intent selfIntent = new Intent(SettingActivity.this, SettingActivity.class);
                                        startActivity(selfIntent);
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SettingActivity.this, "Error occurred :" +error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            mDialog.dismiss();
                            Intent selfIntent = new Intent(SettingActivity.this, SettingActivity.class);
                            startActivity(selfIntent);
                            String error = task.getException().getMessage();
                            Toast.makeText(SettingActivity.this, "Error occurred :" +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else{
                mDialog.dismiss();
                Exception error = result.getError();
                Toast.makeText(this, "Error Occurred "+ error, Toast.LENGTH_SHORT).show();
                sendToMainActivity();
            }
        }
    }



}
