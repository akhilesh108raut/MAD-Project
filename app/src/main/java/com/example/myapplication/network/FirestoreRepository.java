package com.example.myapplication.network;

import com.example.myapplication.models.GameSession;
import com.example.myapplication.models.PDSession;
import com.example.myapplication.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirestoreRepository {
    private static FirestoreRepository instance;
    private final FirebaseFirestore db;

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SESSIONS_REACTION = "reaction_sessions";
    private static final String COLLECTION_SESSIONS_MEMORY = "memory_sessions";
    private static final String COLLECTION_PD_SESSIONS = "pd_motor_sessions";

    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    public Task<Void> saveUser(User user) {
        return db.collection(COLLECTION_USERS).document(user.getUserId()).set(user);
    }

    public Task<DocumentSnapshot> getUserProfile(String userId) {
        return db.collection(COLLECTION_USERS).document(userId).get();
    }

    public Task<DocumentSnapshot> checkUserExists(String userId) {
        return db.collection(COLLECTION_USERS).document(userId).get();
    }

    public Task<Void> saveGameSession(GameSession session) {
        String collection = getCollectionForGame(session.getGameName());
        return db.collection(collection).document().set(session);
    }

    public Task<Void> savePDSession(PDSession session) {
        // Use a document ID that includes the userId to help with rules if needed, 
        // but here we just use auto-id.
        return db.collection(COLLECTION_PD_SESSIONS).document().set(session);
    }

    public Query getUserGameSessions(String userId, String gameName) {
        String collection = getCollectionForGame(gameName);
        return db.collection(collection).whereEqualTo("userId", userId);
    }

    private String getCollectionForGame(String gameName) {
        if ("Memory Match".equalsIgnoreCase(gameName)) {
            return COLLECTION_SESSIONS_MEMORY;
        } else {
            return COLLECTION_SESSIONS_REACTION;
        }
    }
    
    // For debugging: test if we can write to a 'test' collection
    public Task<Void> testWrite() {
        java.util.Map<String, Object> test = new java.util.HashMap<>();
        test.put("test", true);
        return db.collection("test_connection").document().set(test);
    }
}
