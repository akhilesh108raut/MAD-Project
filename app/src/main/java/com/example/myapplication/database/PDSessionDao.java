package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.models.PDSession;

import java.util.List;

@Dao
public interface PDSessionDao {
    @Insert
    void insertSession(PDSession session);

    @Update
    void updateSession(PDSession session);

    @Query("SELECT * FROM pd_sessions WHERE userId = :userId ORDER BY timestamp DESC")
    List<PDSession> getSessionsByUser(String userId);

    @Query("SELECT * FROM pd_sessions WHERE userId = :userId AND riskLevel IS NOT NULL ORDER BY timestamp DESC LIMIT 10")
    List<PDSession> getRecentAnalyzedSessions(String userId);
}
