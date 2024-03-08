package com.example.ilostifind.Adapters;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ilostifind.Comment_Detail_Activity;
import com.example.ilostifind.Objects.Post;
import com.example.ilostifind.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.MyViewHolder> {

    Context mContext;
    List<Post> mData;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    public MyPostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //This is where you inflate the layout (Giving a look of our rows)
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_my_post,parent,false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        //Assigning values to each views and elements on rows we created in the recycler view layout file
        //based on the position of the recycler view

        Post post = mData.get(getItemCount() - 1 - position);

        holder.userrowTv.setText(post.getUsername());
        holder.daterowTv.setText(timestampToString((Long)post.getTimestamp()));
        holder.captionrowTv.setText(post.getTdesc());
        Glide.with(mContext).load(post.getPicture()).into(holder.picpostrowIv);
        holder.catpostrowTv.setText(post.getCatpost());
        holder.statpostrowTv.setText(post.getpStatus());

        // recovered button or function
        // Get the post ID for the clicked post
        String postid = mData.get(getItemCount() - 1 - position).getPostid();

        DatabaseReference clickerIDRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Post").child(postid);

        // Get the current user ID from FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        // Accessing a specific child node of the post
        clickerIDRef.child("clickerID").addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String clickerID = dataSnapshot.getValue(String.class);
               if (clickerID != null && !clickerID.isEmpty()) {
                   // The clickerID child node exists and is populated
                   holder.recoveredButton.setVisibility(View.VISIBLE);
               } else {
                   // The clickerID child node is either null or empty
                   holder.recoveredButton.setVisibility(View.INVISIBLE);
               }
           }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // recovered button
        // give rating to clickerID if category is iLost
        // give rating to userID if category is iFind
        holder.recoveredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the post ID for the clicked post
                String postid = mData.get(getItemCount() - 1 - position).getPostid();

                // Get the clickerID of the post
                DatabaseReference clickerIDRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference().child("Post").child(postid).child("clickerID");

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Clicking YES would update the Star Rating System and Delete the Post.").setTitle("Star Rating & Delete Post");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickerIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String clickerID = dataSnapshot.getValue(String.class);
                                if (clickerID != null && !clickerID.isEmpty()) {
                                    // Get the reference to the level child node of the user
                                    DatabaseReference levelRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                                            .getReference().child("users").child(clickerID).child("level");

                                    // Increment the level value of the user by one based on the post category
                                    DatabaseReference postRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                                            .getReference().child("Post").child(postid);
                                    postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String catpost = dataSnapshot.child("catpost").getValue(String.class);
                                            if (catpost != null && !catpost.isEmpty()) {
                                                if (catpost.equals("iLost")) {
                                                    // Increment the level value of the user by one
                                                    levelRef.runTransaction(new Transaction.Handler() {
                                                        @NonNull
                                                        @Override
                                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                                            Integer currentLevel = mutableData.getValue(Integer.class);
                                                            if (currentLevel == null) {
                                                                mutableData.setValue(1);
                                                            } else {
                                                                mutableData.setValue(currentLevel + 1);
                                                            }
                                                            return Transaction.success(mutableData);
                                                        }

                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                                            if (databaseError != null) {
                                                                showMessage("Error updating level: " + databaseError.getMessage());
                                                            } else {
                                                                showMessage("Level updated successfully! You have given a star!");
                                                            }
                                                        }
                                                    });
                                                } else if (catpost.equals("iFind")) {
                                                    // Get the reference to the level child node of the clickerID user
                                                    DatabaseReference clickerIDLevelRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                                                            .getReference().child("users").child(post.getUserID()).child("level");

                                                    // Increment the level value of the clickerID user by one
                                                    clickerIDLevelRef.runTransaction(new Transaction.Handler() {
                                                        @NonNull
                                                        @Override
                                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                                            Integer currentLevel = mutableData.getValue(Integer.class);
                                                            if (currentLevel == null) {
                                                                mutableData.setValue(1);
                                                            } else {
                                                                mutableData.setValue(currentLevel + 1);
                                                            }
                                                            return Transaction.success(mutableData);
                                                        }

                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                                            if (databaseError != null) {
                                                                showMessage("Error updating level: " + databaseError.getMessage());
                                                            } else {
                                                                showMessage("Level updated successfully! You have been given a star!");
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                            DatabaseReference delpostRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Post").child(postid);
                                            StorageReference delimageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mData.get(position).getPicture());
                                            delpostRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    delimageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mData.remove(position);
                                                            notifyItemRemoved(position);
                                                            notifyItemRangeChanged(position, mData.size());
                                                            Toast.makeText(mContext, "Post deleted.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            showMessage("Error getting post category: " + databaseError.getMessage());
                                        }
                                    });


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                showMessage("Error getting clickerID: " + databaseError.getMessage());
                            }
                        });


                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // delete post
        holder.trasimgBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postid = mData.get(getItemCount() - 1 - position).getPostid();
                String pictureUrl = mData.get(position).getPicture(); // added this line to get picture URL
                Log.d("MyTag", "Post ID: " + postid);
                Log.d("MyTag", "Picture URL: " + pictureUrl);
                DatabaseReference mpostRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Post").child(postid);
                DatabaseReference mcommentRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Comment").child(postid);
                StorageReference imageRef = FirebaseStorage.getInstance("gs://ilostifind-631c2.appspot.com/").getReferenceFromUrl(pictureUrl); // used pictureUrl instead of mData.get(position).getPicture()

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure you want to delete this post?")
                        .setTitle("Delete Post");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked Yes button
                                // Perform the delete operation here
                                mcommentRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mpostRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mData.remove(position);
                                                        notifyItemRemoved(position);
                                                        notifyItemRangeChanged(position, mData.size());
                                                        Toast.makeText(mContext, "Post deleted.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        // The recycler view just wants to know the number of items you want to display
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //Grabbing all the views from our recycler row layout file and assigning values to them
        //Like an onCreate method

        //Declare views
        TextView userrowTv, daterowTv, captionrowTv, catpostrowTv, statpostrowTv;

        Button commentRow, recoveredButton;
        //Button delbutTv;
        ImageButton trasimgBut;
        ImageView picpostrowIv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            userrowTv = itemView.findViewById(R.id.usernamerowmView);
            daterowTv = itemView.findViewById(R.id.daterowmView);
            captionrowTv = itemView.findViewById(R.id.caprowmView);
            catpostrowTv = itemView.findViewById(R.id.catpostrowmView);
            statpostrowTv = itemView.findViewById(R.id.statusrowmView);
            picpostrowIv = itemView.findViewById(R.id.picrowmView);
            commentRow = itemView.findViewById(R.id.commentrowmView); // used for the previous function of picpostrowIv
            trasimgBut = itemView.findViewById(R.id.trashIB);
            recoveredButton = itemView.findViewById(R.id.recoveredBtn);
            //delbutTv = itemView.findViewById(R.id.delbutrowmView);

            commentRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPostDetails();
                }
            });

        }

        private void getPostDetails() {
            Intent comment_Detail_Activity = new Intent(mContext, Comment_Detail_Activity.class);
            int position = getAbsoluteAdapterPosition();// todo eedit this
            Post post = mData.get(getItemCount() - 1 - position);

            comment_Detail_Activity.putExtra("User",post.getUsername());
            comment_Detail_Activity.putExtra("postImage",post.getPicture());
            comment_Detail_Activity.putExtra("description",post.getTdesc());
            comment_Detail_Activity.putExtra("postid",post.getPostid());
            Date timestamp = new Date((long) post.getTimestamp());
            comment_Detail_Activity.putExtra("postDate", timestamp.getTime());
            mContext.startActivity(comment_Detail_Activity);
        }
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("MMMM-dd-yyyy hh:mm a",calendar).toString();
        return date;
    }

    private void showMessage(String text) {
        Toast.makeText(mContext,text,Toast.LENGTH_LONG).show();
    }



}
