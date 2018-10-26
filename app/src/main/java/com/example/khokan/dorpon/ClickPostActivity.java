package com.example.khokan.dorpon;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton, editPostButton;
    private String postKey,post_description,post_image, currentUser,databaseUserId;
    private DatabaseReference postRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

//        initialization
        postImage = findViewById(R.id.clickpostImage);
        postDescription = findViewById(R.id.clickPostDescription);
        editPostButton = findViewById(R.id.editPostButton);
        deletePostButton= findViewById(R.id.deletePostButton);

        postKey = getIntent().getExtras().get("postKey").toString();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        editPostButton.setVisibility(View.INVISIBLE);
        deletePostButton.setVisibility(View.INVISIBLE);

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    post_description = dataSnapshot.child("postDescription").getValue().toString();
                    post_image = dataSnapshot.child("postImage").getValue().toString();
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();


                    postDescription.setText(post_description);
                    Picasso.with(ClickPostActivity.this).load(post_image).into(postImage);

                    if (currentUser.equals(databaseUserId))
                    {
                        editPostButton.setVisibility(View.VISIBLE);
                        deletePostButton.setVisibility(View.VISIBLE);
                    }
                }else{
                    Toast.makeText(ClickPostActivity.this, "Database has no data....", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCurrentPost();
            }
        });
        
        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postRef.removeValue();
                Toast.makeText(ClickPostActivity.this, "Delete post Successfully....", Toast.LENGTH_SHORT).show();
                sendToMainActivity();
            }
        });
    }

    private void editCurrentPost() {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ClickPostActivity.this, R.style.AlertDialogCustom));
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(this);
        inputField.setText(post_description);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postRef.child("postDescription").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully....", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
