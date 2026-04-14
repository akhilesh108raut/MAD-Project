package com.example.myapplication.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.models.GameSession;
import com.example.myapplication.network.FirebaseAuthManager;
import com.example.myapplication.network.FirestoreRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends BaseActivity {

    private GridLayout gridLayout;
    private TextView tvScore, tvLevel;
    
    private final int[] cardResources = {
            R.drawable.game_tile_1, R.drawable.game_tile_2, 
            R.drawable.game_tile_3, R.drawable.game_tile_4,
            R.drawable.game_tile_1, R.drawable.game_tile_2, 
            R.drawable.game_tile_3, R.drawable.game_tile_4
    };

    private List<Integer> cardDeck;
    private List<ImageView> flippedCards = new ArrayList<>();
    private int score = 0;
    private int level = 1;
    private int matchesFound = 0;
    private boolean isProcessing = false;

    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        gridLayout = findViewById(R.id.gridLayout);
        tvScore = findViewById(R.id.tvScore);
        tvLevel = findViewById(R.id.tvLevel);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        startNewGame();
    }

    private void startNewGame() {
        score = 0;
        level = 1;
        setupLevel();
    }

    private void setupLevel() {
        matchesFound = 0;
        flippedCards.clear();
        isProcessing = false;
        
        tvScore.setText("Score: " + score);
        tvLevel.setText("Level: " + level);

        cardDeck = new ArrayList<>();
        for (int res : cardResources) cardDeck.add(res);
        Collections.shuffle(cardDeck);

        gridLayout.removeAllViews();
        for (int i = 0; i < cardDeck.size(); i++) {
            ImageView imageView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            imageView.setLayoutParams(params);
            
            imageView.setBackgroundResource(R.drawable.game_card_back);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(20, 20, 20, 20);
            imageView.setId(i);
            imageView.setOnClickListener(this::onCardClicked);
            
            gridLayout.addView(imageView);
        }
    }

    private void onCardClicked(View v) {
        if (isProcessing) return;
        ImageView iv = (ImageView) v;
        if (flippedCards.contains(iv) || iv.getVisibility() == View.INVISIBLE) return;

        flipCard(iv, cardDeck.get(iv.getId()), () -> {
            flippedCards.add(iv);
            if (flippedCards.size() == 2) {
                checkMatch();
            }
        });
    }

    private void flipCard(ImageView iv, int resId, Runnable onEnd) {
        Animation out = AnimationUtils.loadAnimation(this, R.anim.card_flip_left_out);
        out.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                iv.setImageResource(resId);
                Animation in = AnimationUtils.loadAnimation(MemoryGameActivity.this, R.anim.card_flip_left_in);
                in.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (onEnd != null) onEnd.run();
                    }
                });
                iv.startAnimation(in);
            }
        });
        iv.startAnimation(out);
    }

    private void checkMatch() {
        isProcessing = true;
        ImageView card1 = flippedCards.get(0);
        ImageView card2 = flippedCards.get(1);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (cardDeck.get(card1.getId()).equals(cardDeck.get(card2.getId()))) {
                card1.setVisibility(View.INVISIBLE);
                card2.setVisibility(View.INVISIBLE);
                matchesFound++;
                score += 10 * level;
                tvScore.setText("Score: " + score);

                if (matchesFound == cardDeck.size() / 2) {
                    if (level < 5) {
                        level++;
                        setupLevel();
                    } else {
                        saveGameResult();
                    }
                }
            } else {
                flipBack(card1);
                flipBack(card2);
                score = Math.max(0, score - 2);
                tvScore.setText("Score: " + score);
            }
            flippedCards.clear();
            isProcessing = false;
        }, 800);
    }

    private void flipBack(ImageView iv) {
        Animation out = AnimationUtils.loadAnimation(this, R.anim.card_flip_left_out);
        out.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                iv.setImageResource(0); // Clear image
                Animation in = AnimationUtils.loadAnimation(MemoryGameActivity.this, R.anim.card_flip_left_in);
                iv.startAnimation(in);
            }
        });
        iv.startAnimation(out);
    }

    private void saveGameResult() {
        String uid = authManager.getCurrentUserId();
        if (uid == null) return;

        showLoading("Saving Score...");
        GameSession session = new GameSession(uid, "Memory Match", score, 0);
        repository.saveGameSession(session).addOnCompleteListener(task -> {
            hideLoading();
            showSuccess("Final Score: " + score);
            finish();
        });
    }

    private abstract static class SimpleAnimationListener implements Animation.AnimationListener {
        @Override public void onAnimationStart(Animation animation) {}
        @Override public void onAnimationRepeat(Animation animation) {}
    }
}
