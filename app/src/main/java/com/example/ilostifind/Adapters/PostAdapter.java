package com.example.ilostifind.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.ilostifind.Comment_Detail_Activity;
import com.example.ilostifind.Objects.Post;
import com.example.ilostifind.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    Context mContext;
    List<Post> mData;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //This is where you inflate the layout (Giving a look of our rows)
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_com_post_items,parent,false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Assigning values to each views and elements on rows we created in the recycler view layout file
        //based on the postion of the recycler view

        Post post = mData.get(getItemCount() - 1 - position);
        DatabaseReference mref = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("users").child(post.getUserID()).child("level");

        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object data = snapshot.getValue();
                holder.starcountupTv.setText(data.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.userrowTv.setText(post.getUsername());

        holder.daterowTv.setText(timestampToString((Long)post.getTimestamp()));
        holder.captionrowTv.setText(post.getTdesc());
        Glide.with(mContext).load(post.getPicture()).into(holder.picpostrowIv);
        holder.catpostrowTv.setText(post.getCatpost());
        holder.statpostrowTv.setText(post.getpStatus());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // hide and disable trash button if not admin
        if(!currentUser.getUid().equals("9dyjbw372wQE5f8nxh64cmmll1X2")){
            holder.trasimgBut.setEnabled(false);
            holder.trasimgBut.setVisibility(View.INVISIBLE);
        }

        holder.trasimgBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postid = mData.get(getItemCount() - 1 - position).getPostid();
                DatabaseReference mpostRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Post").child(postid);
                DatabaseReference mcommentRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Comment").child(postid);

                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mData.get(position).getPicture());

                // Check if the current user is the admin
                if (currentUser != null && currentUser.getUid().equals("9dyjbw372wQE5f8nxh64cmmll1X2")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to delete this post?")
                            .setTitle("Delete Post");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked Yes button
                                    // Perform the delete operation here
                                    mcommentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                                                commentSnapshot.getRef().removeValue();
                                            }
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

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // handle error
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
                } else {
                    Toast.makeText(mContext, "You cannot delete this post", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // thisIsMine-foundIt toggleButtons
        String categoryStatus = post.getCatpost();

        // hides thisIsMine-foundIt to owner
        if (post.getUserID().equals(mAuth.getCurrentUser().getUid())) {
            // Hide the toggle button
            holder.categoryButton.setVisibility(View.INVISIBLE);
            holder.cancelButton.setVisibility(View.INVISIBLE);
        }
        else {
            // Show the toggle button
            holder.categoryButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.INVISIBLE);
        }

        //
        if (categoryStatus.equals("iLost")){
            holder.categoryButton.setText("Found it!");
        }else{
            holder.categoryButton.setText("This is Mine!");
        }

        // Get the post ID for the clicked post
        String postid = mData.get(getItemCount() - 1 - position).getPostid();

        DatabaseReference clickerIDRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference().child("Post").child(postid);

        // Accessing a specific child node of the post
        clickerIDRef.child("clickerID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String clickerID = dataSnapshot.getValue(String.class);
                if (clickerID != null && !clickerID.isEmpty()) {
                    // The clickerID child node exists and is populated
                    holder.categoryButton.setVisibility(View.INVISIBLE);
                    holder.cancelButton.setVisibility(View.VISIBLE);

                    if (post.getUserID().equals(mAuth.getCurrentUser().getUid()))
                        holder.cancelButton.setVisibility(View.INVISIBLE);

                } else {
                    // The clickerID child node is either null or empty
                    holder.categoryButton.setVisibility(View.VISIBLE);
                    holder.cancelButton.setVisibility(View.INVISIBLE);

                    if (post.getUserID().equals(mAuth.getCurrentUser().getUid()))
                        holder.categoryButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });

        holder.categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Toggle button is checked", Toast.LENGTH_SHORT).show();

                // Get the post ID for the clicked post
                String postid = mData.get(getItemCount() - 1 - position).getPostid();

                // Get the current user ID from FirebaseAuth
                mAuth = FirebaseAuth.getInstance();
                String uid = mAuth.getCurrentUser().getUid();

                // Update the clickerID field in the database for the corresponding post
                DatabaseReference postRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference()
                        .child("Post").child(postid);
                postRef.child("clickerID").setValue(uid);

                DatabaseReference poststatusupdater = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference()
                        .child("Post").child(postid).child("pStatus");

                //Updating the Status
                if (categoryStatus.equals("iLost")){
                    poststatusupdater.setValue("Found by: " + mUser.getDisplayName());
                }else{
                    poststatusupdater.setValue("Owned by: " + mUser.getDisplayName());
                }

                // Hide the categoryButton and show the cancelButton
                holder.categoryButton.setVisibility(View.INVISIBLE);
                holder.cancelButton.setVisibility(View.VISIBLE);

            }
        });

        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the post ID for the clicked post
                String postid = mData.get(getItemCount() - 1 - position).getPostid();

                // Get the current user ID from FirebaseAuth
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String uid = mAuth.getCurrentUser().getUid();

                // Check if the clickerID is equal to the userID before allowing cancellation
                String clickerID = mData.get(getItemCount() - 1 - position).getClickerID();
                if (!clickerID.equals(uid)) {
                    Toast.makeText(mContext, "You cannot cancel this item as you did not claim it.", Toast.LENGTH_SHORT).show();
                    return;
                } else if(mUser.isAnonymous()) {
                    Toast.makeText(mContext, "Please login to access.", Toast.LENGTH_SHORT).show();

                }

                // Remove the clickerID field from the database for the corresponding post
                DatabaseReference postRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference()
                        .child("Post").child(postid);
                postRef.child("clickerID").setValue("");

                DatabaseReference poststatusupdater = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference()
                        .child("Post").child(postid).child("pStatus");

                //Updating the Status
                if (categoryStatus.equals("iLost")){
                    poststatusupdater.setValue("Found by: ");
                }else{
                    poststatusupdater.setValue("Owned by: ");
                }

                // Hide the cancelButton and show the categoryButton
                holder.cancelButton.setVisibility(View.INVISIBLE);
                holder.categoryButton.setVisibility(View.VISIBLE);
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
        TextView userrowTv, daterowTv, captionrowTv, catpostrowTv, statpostrowTv,starcountupTv;
        ImageView picpostrowIv,starpicIv;
        ImageButton trasimgBut;
        Button commentRow, categoryButton, cancelButton;
        ToggleButton morfTB;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            mUser = mAuth.getCurrentUser();

            userrowTv = itemView.findViewById(R.id.usernamerowView);
            daterowTv = itemView.findViewById(R.id.daterowView);
            captionrowTv = itemView.findViewById(R.id.caprowView);
            catpostrowTv = itemView.findViewById(R.id.catpostrowView);
            statpostrowTv = itemView.findViewById(R.id.statusrowView);
            picpostrowIv = itemView.findViewById(R.id.picrowView);
            commentRow = itemView.findViewById(R.id.commentrowView); // used for the previous function of picpostrowIv
            trasimgBut = itemView.findViewById(R.id.trashIB);
            categoryButton = itemView.findViewById(R.id.categoryBtn);
            cancelButton = itemView.findViewById(R.id.cancelBtn);
            starpicIv = itemView.findViewById(R.id.starPic);
            starcountupTv = itemView.findViewById(R.id.starCountUpdating);


            // commentRow button validation for guest user
            if (mUser == null || mUser.isAnonymous()) {
                commentRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If user is anonymous
                        showMessage("Please login to access.");
                    }
                });
                categoryButton.setEnabled(false);
                cancelButton.setEnabled(false);
            } else {
                commentRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If user is not anonymous
                        getPostDetails();
                    }
                });
            }
        }

        private void getPostDetails() {
            Intent comment_Detail_Activity = new Intent(mContext, Comment_Detail_Activity.class);
            int position = getAbsoluteAdapterPosition();// todo eedit this
            int rposition = getItemCount() - 1 - position;
            Post post = mData.get(getItemCount() - 1 - position);
            comment_Detail_Activity.putExtra("User",post.getUsername());
            comment_Detail_Activity.putExtra("postImage",post.getPicture());
            comment_Detail_Activity.putExtra("description",post.getTdesc());
            comment_Detail_Activity.putExtra("postid",post.getPostid());
            Date timestamp = new Date((long)post.getTimestamp());
            comment_Detail_Activity.putExtra("postDate", timestamp.getTime());
            comment_Detail_Activity.putExtra("position", rposition);
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
