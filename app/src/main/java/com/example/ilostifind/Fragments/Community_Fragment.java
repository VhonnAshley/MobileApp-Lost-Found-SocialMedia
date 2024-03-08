package com.example.ilostifind.Fragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.ilostifind.Adapters.PostAdapter;
import com.example.ilostifind.Objects.Post;
import com.example.ilostifind.R;
import com.example.ilostifind.Write_a_PostActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Community_Fragment extends Fragment {

        Button writebut;

        FirebaseAuth mAuth;
        FirebaseUser mUser;

        RecyclerView postRecyclerView;
        PostAdapter postAdapter;
        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        List<Post> postList;

        public Community_Fragment() {
            // Required empty public constructor
        }

        public static Community_Fragment newInstance(String param1, String param2) {
            Community_Fragment fragment = new Community_Fragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            postList = new ArrayList<>();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View fragmentView = inflater.inflate(R.layout.fragment_community, container, false);
            postRecyclerView = fragmentView.findViewById(R.id.postRV);
            postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            postRecyclerView.setHasFixedSize(true);
            firebaseDatabase = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app");
            databaseReference = firebaseDatabase.getReference("Post");
            writebut = fragmentView.findViewById(R.id.writepost);

            mAuth = FirebaseAuth.getInstance();
            mUser = mAuth.getCurrentUser();

            // writebut button validation for guest user
            if (mUser == null || mUser.isAnonymous()) {
                writebut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If user is anonymous
                        showMessage("Please login to access.");
                    }
                });
            } else {
                //Write Post Functionality
                writebut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If user is not anonymous
                        Intent intent = new Intent(getActivity(), Write_a_PostActivity.class);
                        startActivity(intent);
                    }
                });
            }

            return  fragmentView;

        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Handle back button press in fragment
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        // Close the fragment when back button is pressed
                        getActivity().onBackPressed();
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();

            //Get List from the database
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList.clear(); // Clear the list before adding data
                    for (DataSnapshot postsnap: snapshot.getChildren()){
                        Post post = postsnap.getValue(Post.class);
                        postList.add(post);
                    }
                    

                    // Set the adapter only once (if it's not yet set)
                    if (postAdapter == null) {
                        postAdapter = new PostAdapter(getActivity(), postList);
                        postRecyclerView.setAdapter(postAdapter);
                    } else {
                        // Notify the adapter that the data set has changed
                        postAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        private void showMessage(String text) {
            Toast.makeText(requireContext(),text,Toast.LENGTH_LONG).show();
        }

    }
