package com.example.khokan.dorpon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khokan.dorpon.Model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private NavigationView navigationView;
    private RecyclerView all_users_post_list;
    private Toolbar mToolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,profileImageRef, postsRef,likesRef;
    private StorageReference profileImageStorageRef;
    private String currentUserId;
    private CircleImageView navProfileImage;
    private TextView navUserName, likesView;
    private final static int GALLERY_PICK=1;
    private ProgressDialog mDialog;
    private ImageButton add_new_post,like_button,comment_button;
    private Boolean likesChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.main_container);
        navigationView = findViewById(R.id.navigation_view);
        mToolbar = findViewById(R.id.main_page_toolbar);
        like_button = findViewById(R.id.like_button);
        comment_button = findViewById(R.id.comment_button);
        likesView = findViewById(R.id.display_no_of_likes);

//        Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        profileImageRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        profileImageStorageRef = FirebaseStorage.getInstance().getReference().child("profile Images");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");


        mDialog = new ProgressDialog(this);

        all_users_post_list = findViewById(R.id.all_users_post_list);

        all_users_post_list.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_users_post_list.setLayoutManager(linearLayoutManager);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //        initialize fields
        navProfileImage =navView.findViewById(R.id.nav_profile_image);
        navUserName = navView.findViewById(R.id.nav_user_full_name);
        add_new_post = findViewById(R.id.add_new_post);
        
//        add new post button action
        add_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPostActivity();
            }
        });

//        profile picture change from navigation bar
        navProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });



        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("userFullName"))
                    { 
                        String userName = dataSnapshot.child("userFullName").getValue().toString();
                        navUserName.setText(userName);
                        
                    }
                    if (dataSnapshot.hasChild("profileImages"))
                    {
                        String profileImage = dataSnapshot.child("profileImages").getValue().toString();
                        Picasso.with(MainActivity.this).load(profileImage).placeholder(R.drawable.profile).into(navProfileImage);
                    }else
                    {
                        Toast.makeText(MainActivity.this, "User name does not Exist....", Toast.LENGTH_SHORT).show();
                    }
                    
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                userMenuSelector(item);

                return false;
            }
        });

        displayAllUserposts();
    }

    private void displayAllUserposts() {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                Posts.class,
                R.layout.all_user_post_layout,
                PostsViewHolder.class,
                postsRef
        ) {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

                final String postKey = getRef(position).getKey();
                viewHolder.setFullName(model.getFullName());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setPostDescription(model.getPostDescription());
                viewHolder.setProfileImage(getApplicationContext(), model.getProfileImage());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());

                viewHolder.setLikeButtonStatus(postKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickIntent.putExtra("postKey", postKey);
                        startActivity(clickIntent);
                    }
                });

                viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentIntent.putExtra("postKey", postKey);
                        startActivity(commentIntent);
                    }
                });

                viewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likesChecker=true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (likesChecker.equals(true))
                                {
                                    if (dataSnapshot.child(postKey).hasChild(currentUserId))
                                    {
                                        likesRef.child(postKey).child(currentUserId).removeValue();
                                        likesChecker=false;
                                    }else
                                    {
                                        likesRef.child(postKey).child(currentUserId).setValue(true);
                                        likesChecker=false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        all_users_post_list.setAdapter(firebaseRecyclerAdapter);
    }


    private void sendToPostActivity() {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }

//    crop the Image and upload to storage

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
                StorageReference filePath = profileImageStorageRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            String imageDownloadUrl=task.getResult().getDownloadUrl().toString();
                            userRef.child(currentUserId).child("profileImages").setValue(imageDownloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        mDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Image uploaded successfully....", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        mDialog.dismiss();
                                        String error = task.getException().getMessage();
                                        Toast.makeText(MainActivity.this, "Error occurred :" +error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            mDialog.dismiss();
                            String error = task.getException().getMessage();
                            Toast.makeText(MainActivity.this, "Error occurred :" +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else{
                mDialog.dismiss();
                Exception error = result.getError();
                Toast.makeText(this, "Error Occurred "+ error, Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        {
            sendToLoginActivity();
        }else
        {
            checkUserExistence();
        }
    }

    private void checkUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id))
                {
                    sendToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.nav_profile:
                sendToProfileActivity();
                break;
            case R.id.nav_find_friends:
                sendToFindFriendsActivity();
                break;
            case R.id.nav_firends:
                Toast.makeText(this, "All Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_message:
                Toast.makeText(this, "Your Message", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_post:
                sendToPostActivity();
                break;
            case R.id.nav_setting:
                sendToMainActivity();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                sendToLoginActivity();
                break;

        }

    }

    private void sendToFindFriendsActivity() {
        Intent postIntent = new Intent(MainActivity.this, FindsFriendsActivity.class);
        startActivity(postIntent);
    }

    private void sendToProfileActivity() {
        Intent postIntent = new Intent(MainActivity.this, ProifleActivity.class);
        startActivity(postIntent);
    }

    private void sendToMainActivity() {
        Intent postIntent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(postIntent);
    }
    private void sendToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;

        public PostsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            likePostButton = mView.findViewById(R.id.like_button);
            commentPostButton = mView.findViewById(R.id.comment_button);
            displayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        public void setLikeButtonStatus(final String postkey)
        {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postkey).hasChild(currentUserId))
                    {
                        countLikes = (int) dataSnapshot.child(postkey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(postkey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setFullName(String fullName)
        {
            TextView posterFUllName = mView.findViewById(R.id.post_user_name);
            posterFUllName.setText(fullName);
        }
        public void setDate(String date)
        {
            TextView showPostDate = mView.findViewById(R.id.post_date);
            showPostDate.setText(date);
        }

        public void setTime(String time) {
            TextView showPostTime = mView.findViewById(R.id.post_time);
            showPostTime.setText("   "+time);
        }

        public void setPostDescription(String postDescription) {
            TextView showPostDescrion = mView.findViewById(R.id.post_description);
            showPostDescrion.setText(postDescription);
        }
        public void setPostImage(Context ctx, String postImage) {
            ImageView showPostImage = mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postImage).into(showPostImage);
        }

        public void setProfileImage(Context ctx, String profileImage) {
            CircleImageView showPostProfileImage = mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileImage).into(showPostProfileImage);
        }
    }

}
