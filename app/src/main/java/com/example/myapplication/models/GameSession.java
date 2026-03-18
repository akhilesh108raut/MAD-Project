package com.example.myapplication.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Production-level GameSession model.
 * Uses @ServerTimestamp for accurate cloud-side timing.
 */
@IgnoreExtraProperties
public class GameSession {
    private String userId;
    private String gameName;
    private int score;
    private long reactionTime;
    @ServerTimestamp
    private Date timestamp;

    // Required for Firestore serialization
    public GameSession() {}

    public GameSession(String userId, String gameName, int score, long reactionTime) {
        this.userId = userId;
        this.gameName = gameName;
        this.score = score;
        this.reactionTime = reactionTime;
        // timestamp will be set by the server if not provided, 
        // but we can set it locally for optimistic UI if needed.
    }

    // Getters and Setters
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
