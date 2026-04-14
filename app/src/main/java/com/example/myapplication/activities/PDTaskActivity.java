package com.example.myapplication.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.PDSession;
import com.example.myapplication.network.LocalSessionManager;
import com.example.myapplication.utils.HealthAnalysisUtils;

import java.util.ArrayList;
import java.util.List;

public class PDTaskActivity extends BaseActivity implements SensorEventListener {

    private MotorTaskView motorTaskView;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private List<PDSession.SensorPoint> imuData = new ArrayList<>();
    
    private String currentTaskType = "Spiral";
    private AppDatabase db;
    private LocalSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pd_task);

        db = AppDatabase.getInstance(this);
        sessionManager = LocalSessionManager.getInstance(this);

        motorTaskView = findViewById(R.id.motorTaskView);
        TextView tvInstruction = findViewById(R.id.tvInstruction);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (getIntent() != null) {
            currentTaskType = getIntent().getStringExtra("TASK_TYPE");
            String instruction = getIntent().getStringExtra("INSTRUCTION");
            if (currentTaskType != null) {
                motorTaskView.setTaskType(currentTaskType);
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(currentTaskType);
            }
            if (instruction != null) tvInstruction.setText(instruction);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        btnReset.setOnClickListener(v -> {
            motorTaskView.reset();
            imuData.clear();
        });

        btnSubmit.setOnClickListener(v -> submitTask());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (gyroscope != null) sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            imuData.add(new PDSession.SensorPoint(event.values[0], event.values[1], event.values[2], System.currentTimeMillis()));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void submitTask() {
        List<PDSession.DataPoint> path = motorTaskView.getCollectedPoints();
        if (path.isEmpty()) {
            Toast.makeText(this, "Please perform the task first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            showError("Session expired. Please log in again.");
            return;
        }

        showLoading("Analyzing & Saving...");

        new Thread(() -> {
            try {
                PDSession session = new PDSession();
                session.userId = sessionManager.getUserId();
                session.taskType = currentTaskType;
                session.timestamp = System.currentTimeMillis();
                session.pathData = path;
                session.imuData = new ArrayList<>(imuData);
                
                // Perform analysis immediately before saving
                HealthAnalysisUtils.analyzeSession(session);

                db.pdSessionDao().insertSession(session);

                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    showError("Error: " + e.getMessage());
                });
            }
        }).start();
    }
}
