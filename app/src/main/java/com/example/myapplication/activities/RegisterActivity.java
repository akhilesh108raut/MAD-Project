package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etPassword;
    private TextInputLayout tilPassword;
    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isSocialLogin = false;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        showError("Google sign in failed: " + e.getMessage());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        tilPassword = findViewById(R.id.tilPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        Button btnPhoneSignUp = findViewById(R.id.btnPhoneSignUp);
        View tvLogin = findViewById(R.id.tvLogin);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        checkExistingAuth();

        btnRegister.setOnClickListener(v -> handleRegistration());
        btnGoogleSignUp.setOnClickListener(v -> signInWithGoogle());
        btnPhoneSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhoneLoginActivity.class);
            startActivity(intent);
        });
        
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    private void checkExistingAuth() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            isSocialLogin = true;
            if (etFullName.getText().toString().isEmpty()) etFullName.setText(user.getDisplayName());
            if (etEmail.getText().toString().isEmpty()) etEmail.setText(user.getEmail());
            if (etPhone.getText().toString().isEmpty()) etPhone.setText(user.getPhoneNumber());

            if (!TextUtils.isEmpty(user.getEmail())) etEmail.setEnabled(false);
            if (!TextUtils.isEmpty(user.getPhoneNumber())) etPhone.setEnabled(false);
            
            tilPassword.setVisibility(View.GONE);
        } else {
            isSocialLogin = false;
            etEmail.setEnabled(true);
            etPhone.setEnabled(true);
            tilPassword.setVisibility(View.VISIBLE);
        }
    }

    private void handleRegistration() {
        String name = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Name required");
            return;
        }
        if (username.length() < 3) {
            etUsername.setError("Username too short");
            return;
        }

        if (isSocialLogin) {
            saveProfile(authManager.getCurrentUserId(), name, username, email, phone);
        } else {
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
                showError("Email or Phone required");
                return;
            }
            
            if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                return;
            }
            
            if (password.length() < 6) {
                etPassword.setError("Password min 6 chars");
                return;
            }

            showLoading("Creating account...");
            authManager.register(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    saveProfile(authManager.getCurrentUserId(), name, username, email, phone);
                } else {
                    hideLoading();
                    showError(task.getException().getMessage());
                }
            });
        }
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading("Authenticating with Google...");
        authManager.signInWithGoogle(idToken).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                checkUserExistsAndNavigate();
            } else {
                hideLoading();
                showError("Google Authentication Failed");
            }
        });
    }

    private void checkUserExistsAndNavigate() {
        String uid = authManager.getCurrentUserId();
        repository.checkUserExists(uid).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful() && task.getResult().exists()) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            } else {
                checkExistingAuth();
            }
        });
    }

    private void saveProfile(String uid, String name, String username, String email, String phone) {
        showLoading("Saving profile...");
        User user = new User(uid, name, username, email, phone);
        repository.saveUser(user).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                showSuccess("Welcome!");
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                showError("Firestore Error: " + task.getException().getMessage());
            }
        });
    }
}
