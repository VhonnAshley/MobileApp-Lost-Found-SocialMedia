package com.example.ilostifind;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartUp_Activity extends AppCompatActivity {

    // Variable Declarations
    private Button startupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        // instantiate
        startupBtn = findViewById(R.id.startupBtn);

        startupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginPage = new Intent(getApplicationContext(),Login_Activity.class);
                startActivity(loginPage);
                finish();
            }
        });
    }
}