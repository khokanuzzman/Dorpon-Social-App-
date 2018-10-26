package com.example.khokan.dorpon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity {
    private ImageButton selectPostImage;
    private Button updatePostButton;
    private EditText postDescription;
    private final static int GALLERY_PICK =1;
    private Uri imageUri;
    private String description;
    private StorageReference postImagesReference;
    private DatabaseReference userRef,postRef;
    private FirebaseAuth mAuth;

    private ProgressDialog mDialog;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserId;


    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


//        firebase
        postImagesReference= FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");



        selectPostImage = findViewById(R.id.select_post_image);
        updatePostButton = findViewById(R.id.update_post_button);
        postDescription = findViewById(R.id.update_post_description);

        mDialog = new ProgressDialog(this);

        mToolbar = findViewById(R.id.update_page_abb_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo() {

        description = postDescription.getText().toString();
        if (imageUri == null)
        {
            Toast.makeText(this, "Please Select Image", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please write description...", Toast.LENGTH_SHORT).show();
        }else
        {
            mDialog.setTitle("Add New Post...");
            mDialog.setMessage("Please Wait While we are updating Your new post....");
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();
            storingImagToFirebaseStorage();
        }

    }

    private void storingImagToFirebaseStorage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        StorageReference filePath = postImagesReference.child("Post Images").child(imageUri.getLastPathSegment()+postRandomName+".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful())
                {
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Post Image is Uploaded Successfully....", Toast.LENGTH_SHORT).show();
                    savePostInfoToDatabase();
                }else
                {
                    String error = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error Occurred : "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void savePostInfoToDatabase() {

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    String fullName = dataSnapshot.child("userFullName").getValue().toString();
                    String profileImage = dataSnapshot.child("profileImages").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid",currentUserId);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("postDescription",description);
                    postMap.put("postImage",downloadUrl);
                    postMap.put("fullName",fullName);
                    postMap.put("profileImage", profileImage);
                    postRef.child(currentUserId+postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    
                                    if (task.isSuccessful())
                                    {
                                        mDialog.dismiss();
                                        sendToMainActivity();
                                        Toast.makeText(PostActivity.this, "New Post is Successfully Updated!", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, "Error Occurred : "+error, Toast.LENGTH_SHORT).show();
                                        mDialog.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //intent here to open gallery
    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

//    for activity result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == GALLERY_PICK && resultCode== RESULT_OK && data !=null)
        {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id== android.R.id.home)
        {
            sendToMainActivity();
        }


        return super.onOptionsItemSelected(item);
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
