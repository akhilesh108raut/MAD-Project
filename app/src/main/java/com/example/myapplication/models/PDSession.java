package com.example.myapplication.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "pd_sessions")
public class PDSession {
    @PrimaryKey(autoGenerate = true)
    private int id;
    public String userId;
    public String taskType;
    public long timestamp;
    public List<DataPoint> pathData;
    public List<SensorPoint> imuData;
    
    // Enhanced Analysis Fields
    public double tremorScore;    // 0-100 scale
    public double stabilityScore; // 0-100 scale
    public double riskScore;      // Overall risk score
    public String riskLevel;      // Normal, Mild, Moderate, High
    public String patientSummary; // Patient-friendly explanation

    public PDSession() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public static class DataPoint {
        public float x, y, pressure;
        public long t;
        public DataPoint() {}
        public DataPoint(float x, float y, float p, long t) {
            this.x = x; this.y = y; this.pressure = p; this.t = t;
        }
    }

    public static class SensorPoint {
        public float ax, ay, az;
        public long t;
        public SensorPoint() {}
        public SensorPoint(float x, float y, float z, long t) {
            this.ax = x; this.ay = y; this.az = z; this.t = t;
        }
    }
}
