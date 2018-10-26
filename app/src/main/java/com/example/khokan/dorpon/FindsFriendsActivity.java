package com.example.khokan.dorpon;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindsFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private ImageButton searchButton;
    private EditText searchInputText;
    private RecyclerView searchResultList;
    private DatabaseReference allUserDatabaseRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finds_friends);

        allUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

//        RecyclerView
        searchResultList = findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

//        buttons
        searchButton = findViewById(R.id.search_people_firends_button);
        searchInputText = findViewById(R.id.search_box_input_text);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();
                searchPeopleAndFriends(searchBoxInput);
            }
        });


    }

    private void searchPeopleAndFriends(String searchBoxInput) {
        Toast.makeText(this, "Searching.....", Toast.LENGTH_SHORT).show();
        Query searchQuery = allUserDatabaseRef.orderByChild("userFullName").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                        FindFriends.class,
                        R.layout.all_user_display_layout,
                        FindFriendsViewHolder.class,
                searchQuery

        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, FindFriends model, int position) {

                viewHolder.setUserFullName(model.getUserFullName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfileImages(getApplicationContext(), model.getProfileImages());
            }
        };

        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileImages(Context ctx, String profileImages) {
            CircleImageView myImage = mView.findViewById(R.id.all_user_profile_image);
            Picasso.with(ctx).load(profileImages).placeholder(R.drawable.profile).into(myImage);
        }
        public void setUserFullName(String userFullName) {
            TextView myName = mView.findViewById(R.id.all_user_displayName);
            myName.setText(userFullName);
        }

        public void setStatus(String status) {
            TextView myStatus = mView.findViewById(R.id.all_user_status);
            myStatus.setText(status);
        }
    }
}