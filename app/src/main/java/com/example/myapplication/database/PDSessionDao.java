package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.models.PDSession;

import java.util.List;

@Dao
public interface PDSessionDao {
    @Insert
    void insertSession(PDSession session);

    @Query("SELECT * FROM pd_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    List<PDSession> getSessionsByUser(String userId);
}
