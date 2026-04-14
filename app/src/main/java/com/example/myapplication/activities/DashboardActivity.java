package com.example.myapplication.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.PDSession;
import com.example.myapplication.network.LocalSessionManager;
import com.example.myapplication.utils.HealthAnalysisUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private TextView tvOverallMotorScore, tvPatientSummary;
    private Chip chipRiskLevel;
    private LineChart tremorChart;
    private BarChart stabilityChart;
    private AppDatabase db;
    private LocalSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvOverallMotorScore = findViewById(R.id.tvOverallMotorScore);
        tvPatientSummary = findViewById(R.id.tvPatientSummary);
        chipRiskLevel = findViewById(R.id.chipRiskLevel);
        tremorChart = findViewById(R.id.tremorChart);
        stabilityChart = findViewById(R.id.stabilityChart);

        db = AppDatabase.getInstance(this);
        sessionManager = LocalSessionManager.getInstance(this);

        setupCharts();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Health Trends");
        }
        
        findViewById(R.id.btnShareStats).setOnClickListener(v -> shareStats());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void setupCharts() {
        // Setup Tremor Chart
        tremorChart.getDescription().setEnabled(false);
        tremorChart.setNoDataText("Collect data to see trends");
        XAxis xAxisT = tremorChart.getXAxis();
        xAxisT.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisT.setGranularity(1f);
        
        YAxis leftAxisT = tremorChart.getAxisLeft();
        leftAxisT.setAxisMinimum(0f);
        leftAxisT.setAxisMaximum(100f);
        tremorChart.getAxisRight().setEnabled(false);

        // Setup Stability Chart
        stabilityChart.getDescription().setEnabled(false);
        stabilityChart.setNoDataText("Collect data to see history");
        XAxis xAxisS = stabilityChart.getXAxis();
        xAxisS.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisS.setGranularity(1f);
        
        YAxis leftAxisS = stabilityChart.getAxisLeft();
        leftAxisS.setAxisMinimum(0f);
        leftAxisS.setAxisMaximum(100f);
        stabilityChart.getAxisRight().setEnabled(false);
    }

    private void loadUserData() {
        String uid = sessionManager.getUserId();
        if (uid == null) return;

        new Thread(() -> {
            List<PDSession> pdSessions = db.pdSessionDao().getSessionsByUser(uid);
            
            // Check for missing analysis
            for (PDSession session : pdSessions) {
                if (session.riskLevel == null) {
                    HealthAnalysisUtils.analyzeSession(session);
                    db.pdSessionDao().updateSession(session);
                }
            }

            runOnUiThread(() -> {
                if (pdSessions != null && !pdSessions.isEmpty()) {
                    Toast.makeText(this, "Loaded " + pdSessions.size() + " records", Toast.LENGTH_SHORT).show();
                    updateHealthUI(pdSessions);
                } else {
                    showEmptyState();
                }
            });
        }).start();
    }

    private void showEmptyState() {
        tvOverallMotorScore.setText("--");
        chipRiskLevel.setText("No Data");
        tvPatientSummary.setText("Complete a motor task to generate your first health report.");
        tremorChart.clear();
        stabilityChart.clear();
    }

    private void updateHealthUI(List<PDSession> sessions) {
        PDSession latest = sessions.get(0);
        int overallScore = (int) ((latest.tremorScore + latest.stabilityScore) / 2);
        tvOverallMotorScore.setText(String.valueOf(overallScore));
        
        chipRiskLevel.setText(latest.riskLevel);
        chipRiskLevel.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                HealthAnalysisUtils.getRiskColor(latest.riskLevel)));
        tvPatientSummary.setText(latest.patientSummary);

        displayCharts(sessions);
    }

    private void displayCharts(List<PDSession> sessions) {
        List<Entry> tremorEntries = new ArrayList<>();
        List<BarEntry> stabilityEntries = new ArrayList<>();
        
        int count = Math.min(sessions.size(), 10);
        for (int i = 0; i < count; i++) {
            PDSession s = sessions.get(count - 1 - i);
            tremorEntries.add(new Entry(i, (float) s.tremorScore));
            stabilityEntries.add(new BarEntry(i, (float) s.stabilityScore));
        }

        // Line Chart
        LineDataSet lineSet = new LineDataSet(tremorEntries, "Tremor");
        lineSet.setColor(Color.BLUE);
        lineSet.setCircleColor(Color.BLUE);
        lineSet.setDrawCircles(true);
        lineSet.setCircleRadius(5f);
        lineSet.setLineWidth(3f);
        lineSet.setDrawValues(true);
        
        tremorChart.setData(new LineData(lineSet));
        tremorChart.animateX(500);
        tremorChart.invalidate();

        // Bar Chart
        BarDataSet barSet = new BarDataSet(stabilityEntries, "Stability");
        barSet.setColor(Color.parseColor("#4CAF50"));
        barSet.setDrawValues(true);
        
        stabilityChart.setData(new BarData(barSet));
        stabilityChart.animateY(500);
        stabilityChart.invalidate();
    }

    private void shareStats() {
        String msg = "My NeuroDraw Status: " + tvOverallMotorScore.getText() + "/100 - " + chipRiskLevel.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, "Share My Progress"));
    }
}
