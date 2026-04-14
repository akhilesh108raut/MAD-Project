package com.example.myapplication.database;

import androidx.room.TypeConverter;

import com.example.myapplication.models.PDSession;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromDataPointList(List<PDSession.DataPoint> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<PDSession.DataPoint> toDataPointList(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<PDSession.DataPoint>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromSensorPointList(List<PDSession.SensorPoint> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<PDSession.SensorPoint> toSensorPointList(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<PDSession.SensorPoint>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
}
