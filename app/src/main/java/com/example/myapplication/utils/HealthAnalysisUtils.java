package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.myapplication.models.PDSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class HealthAnalysisUtils {

    // Risk Levels
    public static final String RISK_NORMAL = "Normal";
    public static final String RISK_MILD = "Mild";
    public static final String RISK_MODERATE = "Moderate";
    public static final String RISK_HIGH = "High";

    /**
     * Comprehensive analysis that updates the session with scores and summaries.
     */
    public static void analyzeSession(PDSession session) {
        double tremorIntensity = calculateTremorIntensity(session.imuData);
        double drawingInstability = calculateDrawingInstability(session.pathData);

        // Normalized scores (0-100)
        // Adjust these magic numbers based on real-world calibration
        session.tremorScore = Math.min(100, tremorIntensity * 50); 
        session.stabilityScore = Math.max(0, 100 - (drawingInstability * 20));

        // Hybrid Score for Risk (Weighted)
        double totalRiskScore = (session.tremorScore * 0.7) + ((100 - session.stabilityScore) * 0.3);
        session.riskScore = totalRiskScore;

        if (totalRiskScore < 15) {
            session.riskLevel = RISK_NORMAL;
            session.patientSummary = "🟢 Your hand is steady. No significant shaking detected.";
        } else if (totalRiskScore < 40) {
            session.riskLevel = RISK_MILD;
            session.patientSummary = "🟡 You have slight hand shaking. It is recommended to monitor regularly.";
        } else if (totalRiskScore < 70) {
            session.riskLevel = RISK_MODERATE;
            session.patientSummary = "🟠 Noticeable hand shaking detected. Consider discussing this with your doctor.";
        } else {
            session.riskLevel = RISK_HIGH;
            session.patientSummary = "🔴 Significant hand shaking and movement instability. Please consult a healthcare professional.";
        }
    }

    public static int getRiskColor(String riskLevel) {
        if (riskLevel == null) return Color.GRAY;
        switch (riskLevel) {
            case RISK_NORMAL: return Color.parseColor("#4CAF50"); // Green
            case RISK_MILD: return Color.parseColor("#FFEB3B");   // Yellow
            case RISK_MODERATE: return Color.parseColor("#FF9800"); // Orange
            case RISK_HIGH: return Color.parseColor("#F44336");     // Red
            default: return Color.GRAY;
        }
    }

    /**
     * Estimates tremor intensity from IMU data (Root Mean Square of high-pass).
     */
    public static double calculateTremorIntensity(List<PDSession.SensorPoint> data) {
        if (data == null || data.size() < 2) return 0.0;
        double sumSq = 0;
        for (int i = 1; i < data.size(); i++) {
            float dx = data.get(i).ax - data.get(i - 1).ax;
            float dy = data.get(i).ay - data.get(i - 1).ay;
            float dz = data.get(i).az - data.get(i - 1).az;
            double magnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);
            sumSq += magnitude * magnitude;
        }
        return Math.sqrt(sumSq / data.size());
    }

    /**
     * Estimates instability based on drawing path smoothness/irregularity.
     */
    private static double calculateDrawingInstability(List<PDSession.DataPoint> path) {
        if (path == null || path.size() < 3) return 0.0;
        double jerkiness = 0;
        for (int i = 2; i < path.size(); i++) {
            // Simple second derivative (acceleration/jerk proxy) of position
            float v1x = path.get(i-1).x - path.get(i-2).x;
            float v1y = path.get(i-1).y - path.get(i-2).y;
            float v2x = path.get(i).x - path.get(i-1).x;
            float v2y = path.get(i).y - path.get(i-1).y;
            
            jerkiness += Math.sqrt(Math.pow(v2x - v1x, 2) + Math.pow(v2y - v1y, 2));
        }
        return jerkiness / path.size();
    }

    public static Uri exportSessionToCSV(Context context, PDSession session) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,AX,AY,AZ\n");
        if (session.imuData != null) {
            for (PDSession.SensorPoint p : session.imuData) {
                csv.append(p.t).append(",").append(p.ax).append(",").append(p.ay).append(",").append(p.az).append("\n");
            }
        }
        File file = new File(context.getCacheDir(), "PD_Task_" + session.timestamp + ".csv");
        FileOutputStream out = new FileOutputStream(file);
        out.write(csv.toString().getBytes());
        out.close();
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }
}
