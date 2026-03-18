package com.example.myapplication.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class BaseActivity extends AppCompatActivity {

    private Dialog loadingDialog;

    protected void showLoading(String message) {

        // Prevent multiple dialogs
        hideLoading();

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);

        TextView tvMessage = loadingDialog.findViewById(R.id.tvLoadingMessage);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }

        loadingDialog.show();
    }

    protected void hideLoading() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            loadingDialog = null;
        }
    }

    protected void showError(String message) {
        hideLoading();
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
    }

    protected void showSuccess(String message) {
        hideLoading();
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        hideLoading();  // Prevent window leak
        super.onDestroy();
    }
}