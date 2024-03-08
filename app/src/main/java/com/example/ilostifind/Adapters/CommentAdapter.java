package com.example.ilostifind.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ilostifind.Objects.Comment;
import com.example.ilostifind.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context mContext;
    private List<Comment> mData;

    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment,parent,false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        Comment comment = mData.get(getItemCount() - 1 - position);
        holder.tv_name.setText(comment.getUname());
        holder.tv_content.setText(comment.getContent());
        holder.tv_date.setText(timestampToString((Long)comment.getTimestamp()));
        Glide.with(holder.defaultImg.getContext())
                .load(R.drawable.profile)
                .into(holder.defaultImg);

        // it will only show the delete comment button to the users own
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (comment.getUid().equals(currentUserId) || currentUserId.equals("9dyjbw372wQE5f8nxh64cmmll1X2")) {
            // If so, set the visibility of the trash icon to VISIBLE
            holder.ib_comdel.setVisibility(View.VISIBLE);
        } else {
            // Otherwise, set the visibility of the trash icon to GONE
            holder.ib_comdel.setVisibility(View.INVISIBLE);
        }

        holder.ib_comdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getUid().equals(currentUserId) || currentUserId.equals("9dyjbw372wQE5f8nxh64cmmll1X2")) {
                    Snackbar snackbar = Snackbar.make(v, "Are you sure you want to delete this comment?", Snackbar.LENGTH_LONG)
                            .setAction("Delete", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Perform the delete action
                                    // Delete function
                                    String postid = mData.get(getItemCount() - 1 - position).getPostid();
                                    String commentid = mData.get(getItemCount() - 1 - position).getComid();

                                    DatabaseReference commpostref = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                                            .getReference().child("Comment")
                                            .child(postid).child(commentid);
                                    commpostref.removeValue();
                                    mData.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mData.size());
                                    Toast.makeText(mContext, "Comment deleted.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    snackbar.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            snackbar.dismiss();
                        }
                    }, 5000);
                } else {
                    Toast.makeText(mContext, "You cannot delete this comment", Toast.LENGTH_SHORT).show();
                }
            }// end of onclick
        });// end of set onclicklistener



    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name,tv_date,tv_content;
        ImageView defaultImg;
        ImageButton ib_comdel;


        public CommentViewHolder(View itemView){
            super(itemView);
            tv_content = itemView.findViewById(R.id.comment_content);
            tv_date = itemView.findViewById(R.id.comment_date);
            tv_name = itemView.findViewById(R.id.comment_username);
            ib_comdel = itemView.findViewById(R.id.comdeleteIB);
            defaultImg = itemView.findViewById(R.id.imagemView3);
        }

    }
    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm",calendar).toString();
        return date;
    }

    private void showMessage(String text) {
        Toast.makeText(mContext,text,Toast.LENGTH_LONG).show();
    }



}