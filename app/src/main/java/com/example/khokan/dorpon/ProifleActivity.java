package com.example.khokan.dorpon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProifleActivity extends AppCompatActivity {
    private TextView setting_user_status,setting_user_name,setting_user_full_name, setting_user_country, setting_user_gender, setting_user_date_of_birth, setting_relationship_status;
    private CircleImageView myProfileImage;
    private DatabaseReference profileRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proifle);

//        Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


        //        Initialization
        setting_user_status = findViewById(R.id.my_profile_status);
        setting_user_full_name = findViewById(R.id.my_profile_full_name);
        setting_user_country = findViewById(R.id.my_country);
        setting_user_date_of_birth= findViewById(R.id.my_date_of_birth);
        setting_relationship_status = findViewById(R.id.my_relationship_status);
        setting_user_gender= findViewById(R.id.my_gender);
        myProfileImage = findViewById(R.id.my_profile_pic);
        setting_user_name =findViewById(R.id.my_profile_user_name);

        profileRef.addValueEventListener(new ValueEventListener() {
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
                    setting_user_country.setText("Country: "+userCountry);
                    setting_user_date_of_birth.setText("Birth: "+userBirth);
                    setting_relationship_status.setText("Relationship: "+userRelationship);
                    setting_user_gender.setText("Gender: "+userGender);
                    setting_user_name.setText("@"+userName);
                    Picasso.with(ProifleActivity.this).load(userProfileImage).placeholder(R.drawable.profile).into(myProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
