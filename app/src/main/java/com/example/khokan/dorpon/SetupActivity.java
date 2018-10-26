package com.example.khokan.dorpon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText setupUserName, setupUserFullName, setupUserCountry;
    private CircleImageView userSetupprofileImage;
    private Button userSetupInformationButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;

    private String currentUserId;
    private ProgressDialog mDialog;
    private final static int GALLERY_PICK=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
//      firebase initialize
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");


        setupUserName = findViewById(R.id.setup_username);
        setupUserFullName = findViewById(R.id.setup_fullname);
        setupUserCountry = findViewById(R.id.setup_country);
        userSetupInformationButton = findViewById(R.id.setup_info_save_btn);
        userSetupprofileImage = findViewById(R.id.setup_profile_image);

        mDialog = new ProgressDialog(this);

        userSetupInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSetupInformation();
            }

        });

        userSetupprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("profileImages"))
                {
                    String image = dataSnapshot.child("profileImages").getValue().toString();
                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(userSetupprofileImage);
                }else 
                {
                    Toast.makeText(SetupActivity.this, "Please select Profile image first...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                            userRef.child("profileImages").setValue(imageDownloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        mDialog.dismiss();
                                        senToSetupToSetup();
                                        Toast.makeText(SetupActivity.this, "Image uploaded successfully....", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        mDialog.dismiss();
                                        senToSetupToSetup();
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error occurred :" +error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            mDialog.dismiss();
                            senToSetupToSetup();
                            String error = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error occurred :" +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else{
                mDialog.dismiss();
                Exception error = result.getError();
                Toast.makeText(this, "Error Occurred "+ error, Toast.LENGTH_SHORT).show();
                senToSetupToSetup();
            }
        }
    }

//    show image after set image

    private void userSetupInformation() {
        String userName = setupUserName.getText().toString();
        String userFullName = setupUserFullName.getText().toString();
        String userCountry = setupUserCountry.getText().toString();

        if (TextUtils.isEmpty(userName))
        {
            Toast.makeText(this, "Please write Your User Name..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userCountry))
        {
            Toast.makeText(this, "Please write Your country name..", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userFullName))
        {
            Toast.makeText(this, "Please write your full name..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap userSetupMap = new HashMap();
            userSetupMap.put("userName",userName);
            userSetupMap.put("userFullName", userFullName);
            userSetupMap.put("userCountry", userCountry);
            userSetupMap.put("status", "Hi There i,m using Dorpon the Social App");
            userSetupMap.put("gender", "none");
            userSetupMap.put("dob", "none");
            userSetupMap.put("relationship", "none");

            mDialog.setTitle("Update Info");
            mDialog.setMessage("Please Wait While we are updating Your account....");
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();

            userRef.updateChildren(userSetupMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        mDialog.dismiss();
                        sendToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account Successfully Created!", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        mDialog.dismiss();
                        String Error = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured: "+Error, Toast.LENGTH_SHORT).show();

                    }
                }
            });


        }
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void senToSetupToSetup()
    {
        Intent setupIntent = new Intent(SetupActivity.this, SetupActivity.class);
        startActivity(setupIntent);
    }
}
