package com.example.ilostifind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ilostifind.Objects.Comment;

import com.bumptech.glide.Glide;
import com.example.ilostifind.Adapters.CommentAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Comment_Detail_Activity extends AppCompatActivity {

    ImageView imgPost, defaultImg1, defaultImg2;
    TextView txtPostDesc, txtPostDate, txtName;
    EditText editTextComment;
    Button btnAddComment;
    String PostKey;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "Comment" ;
    private int mPosition;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_detail);

        // ini views
        RvComment = findViewById(R.id.rv_comment);
        imgPost = findViewById(R.id.post_detail_img);
        defaultImg1 = findViewById(R.id.imgV);
        defaultImg2 = findViewById(R.id.post_detail_currentuser_img);

        txtName = findViewById(R.id.post_user);
        txtPostDesc = findViewById(R.id.post_detail_desc);
        txtPostDate = findViewById(R.id.post_detail_date);

        editTextComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app/");


        //String postKey = postsRef.push().getKey();
        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String postUser = getIntent().getExtras().getString("User");
        txtName.setText(postUser);

        String postDescription = getIntent().getExtras().getString("description");
        txtPostDesc.setText(postDescription);

        // get post id
        PostKey = getIntent().getExtras().getString("postid");

        String date = timestampToString(getIntent().getExtras().getLong("postDate"));
        txtPostDate.setText(date);

        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PostKey != null) {
                    if(editTextComment.getText().toString().isEmpty()) {
                        showMessage("Please enter all fields!");
                    } else {
                        btnAddComment.setVisibility(View.INVISIBLE);
                        Comment comment = new Comment(editTextComment.getText().toString(),
                                firebaseUser.getUid(), firebaseUser.getDisplayName(), PostKey);
                        addComment(comment);
                    }
                } else {
                    showMessage("Post key is null");
                }
            }
        });

        // ini Recyclerview Comment
        iniRvComment();
    }

    private void iniRvComment() {
        RvComment.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference commentRef = firebaseDatabase.getReference("Comment").child(PostKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listComment = new ArrayList<>();
                    for (DataSnapshot snap: snapshot.getChildren()){

                        Comment comment = snap.getValue(Comment.class);
                        listComment.add(comment);

                    }
                    commentAdapter = new CommentAdapter(getApplicationContext(), listComment);
                    RvComment.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addComment(Comment comment) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference comyRef = database.getReference("Comment").child(PostKey).push();

        String key = comyRef.getKey();
        comment.setComid(key);

        comyRef.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showMessage("Comment added successfully.");
                editTextComment.setText("");
                btnAddComment.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage("Fail to add comment: "+e.getMessage());
            }
        });


    }

    @Override
    public void onBackPressed() {
        finish();

    }


    private void showMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("MMMM-dd-yyyy hh:mm a",calendar).toString();
        return date;
    }
}