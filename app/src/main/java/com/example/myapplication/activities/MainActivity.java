package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.network.FirebaseAuthManager;

/**
 * Production-level MainActivity.
 * Handles splash/initial routing logic based on Auth state.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in for immediate redirection
        if (FirebaseAuthManager.getInstance().isUserLoggedIn()) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button login = findViewById(R.id.btnLogin);
        Button register = findViewById(R.id.btnRegister);

        login.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        register.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
