package com.example.ilostifind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ilostifind.Fragments.Community_Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_Activity extends AppCompatActivity {

    // declarations
    private EditText loginEmail, loginPassword;
    private Button loginUserBtn, loginGuestBtn, loginSignUpBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // instantiate
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginUserBtn = findViewById(R.id.loginUser);
        loginGuestBtn = findViewById(R.id.loginGuestBtn);
        loginSignUpBtn = findViewById(R.id.loginSignupBtn);

        mAuth = FirebaseAuth.getInstance();

        loginUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        loginGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginGuest();
                loginGuestBtn.setVisibility(View.INVISIBLE);
            }
        });

        loginSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login_Activity.this, Register_Activity.class));
            }
        });
    }

    private void loginGuest() {
        loginGuestBtn.setVisibility(View.INVISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(Login_Activity.this, Home_Activity.class));
                            showMessage("Logged in as guest.");
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login_Activity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            loginGuestBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void loginUser() {
        // declarations
        final String email = loginEmail.getText().toString();
        final String password = loginPassword.getText().toString();

        loginUserBtn.setVisibility(View.INVISIBLE);

        // validations
        if(email.isEmpty() || password.isEmpty()) {
            // error: all fields must be answered
            showMessage("Please verify all fields!"); // display an error message
            loginUserBtn.setVisibility(View.VISIBLE);
        }
        else {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        showMessage("User logged in successfully!");
                        startActivity(new Intent(Login_Activity.this, Home_Activity.class));
                    }
                    else {
                        showMessage("Log in error " + task.getException().getMessage());
                        loginUserBtn.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

}