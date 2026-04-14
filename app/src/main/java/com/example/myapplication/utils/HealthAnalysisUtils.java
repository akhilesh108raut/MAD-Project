package com.example.myapplication.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.myapplication.models.PDSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class HealthAnalysisUtils {

    /**
     * Simple Tremor Analysis: Calculates the Root Mean Square (RMS) of the 
     * high-pass filtered accelerometer data to estimate tremor intensity.
     */
    public static double calculateTremorIntensity(List<PDSession.SensorPoint> data) {
        if (data == null || data.size() < 2) return 0.0;

        double sumSq = 0;
        // Simple high-pass filter: (current - previous)
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
     * Generates a CSV file for a PDSession and returns the Uri for sharing.
     */
    public static Uri exportSessionToCSV(Context context, PDSession session) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,AX,AY,AZ\n");
        
        for (PDSession.SensorPoint p : session.imuData) {
            csv.append(p.t).append(",")
               .append(p.ax).append(",")
               .append(p.ay).append(",")
               .append(p.az).append("\n");
        }

        String fileName = "PD_Task_" + session.taskType + "_" + session.timestamp + ".csv";
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) exportDir.mkdirs();
        
        File file = new File(exportDir, fileName);
        FileOutputStream out = new FileOutputStream(file);
        out.write(csv.toString().getBytes());
        out.close();

        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }
}
