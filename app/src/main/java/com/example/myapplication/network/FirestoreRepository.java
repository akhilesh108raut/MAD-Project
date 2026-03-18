package com.example.myapplication.network;

import com.example.myapplication.models.GameSession;
import com.example.myapplication.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Singleton repository for Firestore operations.
 * Centralizes all data access logic for clean architecture.
 */
public class FirestoreRepository {
    private static FirestoreRepository instance;
    private final FirebaseFirestore db;

    // Collection Constants
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SESSIONS = "game_sessions";

    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    // --- User Operations ---

    /**
     * Saves or updates a user profile.
     */
    public Task<Void> saveUser(User user) {
        return db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }

    /**
     * Fetches a user profile by ID.
     */
    public Task<DocumentSnapshot> getUserProfile(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .get();
    }

    /**
     * Checks if a user document exists in Firestore.
     */
    public Task<DocumentSnapshot> checkUserExists(String userId) {
        return db.collection(COLLECTION_USERS).document(userId).get();
    }

    // --- Game Session Operations ---

    /**
     * Saves a new game session.
     */
    public Task<Void> saveGameSession(GameSession session) {
        // Use an auto-generated ID for each session
        return db.collection(COLLECTION_SESSIONS)
                .document()
                .set(session);
    }

    /**
     * Gets all game sessions for a specific user, ordered by most recent.
     */
    public Query getUserGameSessions(String userId) {
        return db.collection(COLLECTION_SESSIONS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    /**
     * Gets sessions for a specific user and game, ordered by score for leaderboards/stats.
     */
    public Query getGameHighScores(String userId, String gameName) {
        return db.collection(COLLECTION_SESSIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("gameName", gameName)
                .orderBy("score", Query.Direction.DESCENDING);
    }

    /**
     * Gets global high scores for a specific game (Leaderboard system).
     */
    public Query getGlobalLeaderboard(String gameName, int limit) {
        return db.collection(COLLECTION_SESSIONS)
                .whereEqualTo("gameName", gameName)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit);
    }
}
