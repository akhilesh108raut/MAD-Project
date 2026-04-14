package com.example.myapplication.models;

import java.util.List;

public class PDSession {
    public String userId;
    public String taskType; // "Spiral" or "Maze"
    public long timestamp;
    public List<DataPoint> pathData;
    public List<SensorPoint> imuData;
    public double riskScore;

    public static class DataPoint {
        public float x, y, pressure;
        public long t;
        public DataPoint(float x, float y, float p, long t) {
            this.x = x; this.y = y; this.pressure = p; this.t = t;
        }
    }

    public static class SensorPoint {
        public float ax, ay, az;
        public long t;
        public SensorPoint(float x, float y, float z, long t) {
            this.ax = x; this.ay = y; this.az = z; this.t = t;
        }
    }
}
