package com.example.myapplication.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.models.PDSession;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class PDTaskActivity extends BaseActivity implements SensorEventListener {

    private MotorTaskView motorTaskView;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private List<PDSession.SensorPoint> imuData = new ArrayList<>();
    
    private String currentTaskType = "Spiral";
    private FirestoreRepository repository;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pd_task);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        motorTaskView = findViewById(R.id.motorTaskView);
        TextView tvInstruction = findViewById(R.id.tvInstruction);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // Setup Sensors (Aiming for 50Hz+ with SENSOR_DELAY_GAME)
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
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
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

        showLoading("Analyzing motor patterns...");

        PDSession session = new PDSession();
        session.userId = authManager.getCurrentUserId();
        session.taskType = currentTaskType;
        session.timestamp = System.currentTimeMillis();
        session.pathData = path;
        session.imuData = new ArrayList<>(imuData);
        
        // Calculate a dummy risk score based on jerk/variance for now
        session.riskScore = calculateRiskScore(path);

        repository.savePDSession(session).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Task submitted successfully", Toast.LENGTH_LONG).show();
                finish();
            } else {
                showError("Submission failed: " + task.getException().getMessage());
            }
        });
    }

    private double calculateRiskScore(List<PDSession.DataPoint> points) {
        if (points.size() < 3) return 0.0;
        
        double totalJerk = 0;
        for (int i = 2; i < points.size(); i++) {
            double dt = (points.get(i).t - points.get(i-1).t) / 1000.0;
            if (dt <= 0) continue;
            
            double vx1 = (points.get(i-1).x - points.get(i-2).x) / dt;
            double vx2 = (points.get(i).x - points.get(i-1).x) / dt;
            double ax = (vx2 - vx1) / dt;
            
            totalJerk += Math.abs(ax);
        }
        
        // Normalize and scale (Mock logic for screening visualization)
        return Math.min(1.0, totalJerk / (points.size() * 100.0));
    }
}
