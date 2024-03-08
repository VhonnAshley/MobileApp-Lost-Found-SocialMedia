package com.example.ilostifind.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ilostifind.Login_Activity;
import com.example.ilostifind.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile_Fragment extends Fragment {

    //declarations
    Button profileFragmentLogoutBtn, cancel_button, confirm_button;
    ImageButton showViewImgButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private TextView mEmailTextView, mPasswordTextView, mNameTextView, starRatingSystem;
    private EditText popNewPassword, popConfirmPassword;
    private TextView textViewNewPass, textViewConfirmPass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Button
        profileFragmentLogoutBtn = view.findViewById(R.id.profileFragmentLogoutBtn);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Find the TextViews in the layout
        mEmailTextView = view.findViewById(R.id.email_text_view);
        mPasswordTextView = view.findViewById(R.id.password_text_view);
        mNameTextView = view.findViewById(R.id.name_text_view);
        showViewImgButton = view.findViewById(R.id.showViewImgButton);

        textViewNewPass = view.findViewById(R.id.textViewNewPass);
        textViewConfirmPass = view.findViewById(R.id.textViewConfirmPass);
        popNewPassword = view.findViewById(R.id.newPass_byUser);
        popConfirmPassword = view.findViewById(R.id.confirmPass_byUser);
        cancel_button = view.findViewById(R.id.cancel_button);
        confirm_button = view.findViewById(R.id.confirm_button);
        starRatingSystem = view.findViewById(R.id.starLevelCount);

        // Get the current user
        mUser = mAuth.getCurrentUser();

        // Get the reference to the level child node of the current user
        DatabaseReference levelRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("users").child(mUser.getUid()).child("level");

        // Retrieve the level value of the current user
        levelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer level = dataSnapshot.getValue(Integer.class);
                if (level != null) {
                    // Convert the level integer to a string and set it to starRatingSystem
                    starRatingSystem.setText(String.valueOf(level));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showMessage("Error getting level: " + databaseError.getMessage());
            }
        });



        if (mUser == null || mUser.isAnonymous()) {
            String email = "Guest";
            String password = "**********"; // Password is not retrievable from Firebase Authentication
            String name = "Guest";

            // Set the email and name in the TextViews
            mEmailTextView.setText(email);
            mPasswordTextView.setText(password);
            mNameTextView.setText(name);

            hideChangePasswordFields();
            showViewImgButton.setEnabled(false);
        } else {
            // Extract the email, password, and display name of the current user
            String email = mUser.getEmail();
            String password = "**********"; // Password is not retrievable from Firebase Authentication
            String name = mUser.getDisplayName();

            // Set the email and name in the TextViews
            mEmailTextView.setText(email);
            mPasswordTextView.setText(password);
            mNameTextView.setText(name);

            // Set all text-views-buttons for changing password to invisible
            textViewNewPass.setVisibility(View.INVISIBLE);
            textViewConfirmPass.setVisibility(View.INVISIBLE);
            popNewPassword.setVisibility(View.INVISIBLE);
            popConfirmPassword.setVisibility(View.INVISIBLE);
            cancel_button.setVisibility(View.INVISIBLE);
            confirm_button.setVisibility(View.INVISIBLE);
        }

        // used for changing password
        showViewImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show all texts and fields for changing password
                showChangePasswordFields();

                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Set all text-views-buttons for changing password to invisible
                        hideChangePasswordFields();
                    }
                });

                confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Set all text-views-buttons for changing password to visible
                        updatePasswordOnVisible();
                    }
                });
            }
        });

        // used for logging out
        profileFragmentLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent loginActivity = new Intent(requireContext(),Login_Activity.class);
                startActivity(loginActivity);
                requireActivity().finish();
                profileFragmentLogoutBtn.setVisibility(View.INVISIBLE);
            }
        });
        return view;
    }

    private void showChangePasswordFields() {
        textViewNewPass.setVisibility(View.VISIBLE);
        textViewConfirmPass.setVisibility(View.VISIBLE);
        popNewPassword.setVisibility(View.VISIBLE);
        popConfirmPassword.setVisibility(View.VISIBLE);
        cancel_button.setVisibility(View.VISIBLE);
        confirm_button.setVisibility(View.VISIBLE);
    }

    private void hideChangePasswordFields() {
        textViewNewPass.setVisibility(View.INVISIBLE);
        textViewConfirmPass.setVisibility(View.INVISIBLE);
        popNewPassword.setVisibility(View.INVISIBLE);
        popConfirmPassword.setVisibility(View.INVISIBLE);
        cancel_button.setVisibility(View.INVISIBLE);
        confirm_button.setVisibility(View.INVISIBLE);
        popNewPassword.setText("");
        popConfirmPassword.setText("");
    }

    private void updatePasswordOnVisible() {
        // Codes-Declarations for the New Password validations
        final String newPassword = popNewPassword.getText().toString();
        final String confirmPassword = popConfirmPassword.getText().toString();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please input all fields!");
        } else if (!confirmPassword.equals(newPassword)) {
            showMessage("Passwords not matching! Try again.");
            popNewPassword.setText("");
            popConfirmPassword.setText("");
        } else {
            mUser.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showMessage("Password successfully changed!");
                            } else {
                                showMessage("Failed to change password!");
                                popNewPassword.setText("");
                                popConfirmPassword.setText("");
                                showChangePasswordFields();
                            }
                        }
                    });
            hideChangePasswordFields();
        }
    }

    private void showMessage(String text) {
        Toast.makeText(requireContext(),text,Toast.LENGTH_LONG).show();
    }

}