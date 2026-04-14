package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.models.GameSession;

import java.util.List;

@Dao
public interface GameSessionDao {
    @Insert
    void insertSession(GameSession session);

    @Query("SELECT * FROM game_sessions WHERE userId = :userId AND gameName = :gameName ORDER BY timestamp DESC")
    List<GameSession> getSessionsByUserAndGame(String userId, String gameName);
}
