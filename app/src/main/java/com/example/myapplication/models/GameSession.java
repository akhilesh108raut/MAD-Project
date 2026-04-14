package com.example.myapplication.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Room Entity for GameSession.
 */
@Entity(tableName = "game_sessions")
public class GameSession {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;
    private String gameName;
    private int score;
    private long reactionTime;
    private Date timestamp;

    public GameSession() {}

    public GameSession(String userId, String gameName, int score, long reactionTime) {
        this.userId = userId;
        this.gameName = gameName;
        this.score = score;
        this.reactionTime = reactionTime;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public long getReactionTime() { return reactionTime; }
    public void setReactionTime(long reactionTime) { this.reactionTime = reactionTime; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
