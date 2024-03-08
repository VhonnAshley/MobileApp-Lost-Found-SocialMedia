package com.example.ilostifind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Toast;

import com.example.ilostifind.Fragments.Community_Fragment;
import com.example.ilostifind.Fragments.Post_Fragment;
import com.example.ilostifind.Fragments.Profile_Fragment;
import com.example.ilostifind.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home_Activity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser == null || mCurrentUser.isAnonymous()) {
            // If user is anonymous (guest), show only the Community Fragment and LoginPage
            replaceFragment(new Community_Fragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.community);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.community) {
                    replaceFragment(new Community_Fragment());
                } else if (item.getItemId() == R.id.my_post) {
                    showMessage("Please login to access.");
                } else if (item.getItemId() == R.id.profile) {
                    replaceFragment(new Profile_Fragment());
                }
                return true;
            });
        } else {
            // If user is not anonymous, show the full bottom navigation view
            replaceFragment(new Community_Fragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.community);
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.community) {
                    replaceFragment(new Community_Fragment());
                } else if (item.getItemId() == R.id.my_post) {
                    replaceFragment(new Post_Fragment());
                } else if (item.getItemId() == R.id.profile) {
                    replaceFragment(new Profile_Fragment());
                }
                return true;
            });
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.addToBackStack(null); // add old fragment to back stack
        fragmentTransaction.commit();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

}