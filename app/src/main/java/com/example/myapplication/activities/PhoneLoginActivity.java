package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends BaseActivity {

    private TextInputEditText etPhoneNumber, etOtp;
    private LinearLayout phoneInputLayout, otpInputLayout;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        otpInputLayout = findViewById(R.id.otpInputLayout);
        Button btnSendOtp = findViewById(R.id.btnSendOtp);
        Button btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        btnSendOtp.setOnClickListener(v -> sendVerificationCode());
        btnVerifyOtp.setOnClickListener(v -> verifyCode());
    }

    private void sendVerificationCode() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }

        showLoading("Sending OTP...");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(authManager.getFirebaseAuth())
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = etOtp.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            etOtp.setError("OTP is required");
            return;
        }

        showLoading("Verifying...");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhone(credential);
    }

    private void signInWithPhone(PhoneAuthCredential credential) {
        authManager.signInWithPhone(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                checkUserExistsAndNavigate();
            } else {
                hideLoading();
                showError("Verification failed: " + task.getException().getMessage());
            }
        });
    }

    private void checkUserExistsAndNavigate() {
        String uid = authManager.getCurrentUserId();
        repository.checkUserExists(uid).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful() && task.getResult().exists()) {
                // User exists, go to Dashboard
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // User doesn't exist, go to Registration
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            signInWithPhone(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            hideLoading();
            showError("Verification failed: " + e.getMessage());
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            hideLoading();
            mVerificationId = verificationId;
            mResendToken = token;
            
            phoneInputLayout.setVisibility(View.GONE);
            otpInputLayout.setVisibility(View.VISIBLE);
            showSuccess("OTP Sent");
        }
    };
}
