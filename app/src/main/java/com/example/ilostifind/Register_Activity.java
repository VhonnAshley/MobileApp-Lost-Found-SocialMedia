package com.example.ilostifind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ilostifind.Objects.users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register_Activity extends AppCompatActivity {

    // declarations
    private EditText userUsername, userPassword, userPassword2, userFullName;
    private Button registerButton, clearAllButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // instantiate
        userUsername = findViewById(R.id.registerEmail);
        userPassword = findViewById(R.id.registerPassword);
        userPassword2 = findViewById(R.id.registerPassword2);
        userFullName = findViewById(R.id.registerFullName);
        registerButton = findViewById(R.id.registerButton);
        clearAllButton = findViewById(R.id.clearBtn);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFields();
            }
        });

    }

    private void clearAllFields() {
        clearAllButton.setVisibility(View.INVISIBLE);
        userUsername.setText("");
        userPassword.setText("");
        userPassword2.setText("");
        userFullName.setText("");
        showMessage("All fields cleared!");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                clearAllButton.setVisibility(View.VISIBLE);
            }
        }, 2000); // Delay in milliseconds (2pass seconds in this case)
    }

    private void createUser() {
        // declarations
        final String email = userUsername.getText().toString();
        final String password = userPassword.getText().toString();
        final String password2 = userPassword2.getText().toString();
        final String fullName = userFullName.getText().toString();

        registerButton.setVisibility(View.INVISIBLE);

        // validations
        if (email.isEmpty() || password.isEmpty() || password2.isEmpty() || fullName.isEmpty()) {
            // error: all fields must be answered
            showMessage("Please verify all fields!"); // display an error message
            registerButton.setVisibility(View.VISIBLE);
        } else if (!password.equals(password2)) {
            // error: password not equal
            showMessage("Passwords not matching!"); // display an error message
            registerButton.setVisibility(View.VISIBLE);
        } else {
            // if all goes well - createUserAccount!
            CreateUserAccount(email,password,fullName);
        }
    }

    private void CreateUserAccount(String email, String password, String fullName) {
        // this method creates user account with specific email and password
        // this actually user firebaseAuth already, which can be seen in the global declarations
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showMessage("Account created!");
                            // after account creation, we have to update user's profile name
                            updateUserInfo(fullName, mAuth.getCurrentUser());
                            mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                                @Override
                                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        // create a new node in the database for this user
                                        DatabaseReference userRef = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app")
                                                .getReference().child("users").child(userId);
                                        userRef.child("level").setValue(0);

                                    }
                                }
                            });
                        }
                        else {
                            showMessage("Account creation failed!" + task.getException().getMessage());
                            registerButton.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    // updates user full name
    private void updateUserInfo(String fullName, FirebaseUser currentUser) {
        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        currentUser.updateProfile(profleUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // user info updated successfully
                            showMessage("Register Complete");
                            updateUI();
                        }

                    }
                });
    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(),Home_Activity.class);
        startActivity(homeActivity);
        finish();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

}
