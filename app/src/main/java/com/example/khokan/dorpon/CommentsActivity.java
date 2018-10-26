package com.example.khokan.dorpon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentsList;
    private ImageButton postCommentButton;
    private EditText postCommentInputText;

    private String post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentsList = findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);

        postCommentInputText = findViewById(R.id.comment_input_text);
        postCommentButton = findViewById(R.id.post_comment_button);

        post_key = getIntent().getExtras().get("postKey").toString();

    }
}
