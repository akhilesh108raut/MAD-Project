package com.example.myapplication.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

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
            R.drawable.ic_star, R.drawable.ic_triangle, 
            R.drawable.ic_spade, R.drawable.ic_heart,
            R.drawable.game_tile_1, R.drawable.game_tile_2, 
            R.drawable.game_tile_4
    };

    private final int[] cardColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.parseColor("#FFA500"),
            Color.parseColor("#800080"), Color.parseColor("#008080"),
            Color.parseColor("#FFC0CB"), Color.parseColor("#A52A2A"),
            Color.parseColor("#7FFF00")
    };

    private List<CardData> cardDeck;
    private List<ImageView> flippedCards = new ArrayList<>();
    private int score = 0;
    private int level = 1;
    private int matchesFound = 0;
    private boolean isProcessing = false;
    private static final int MAX_LEVEL = 10;

    private FirebaseAuthManager authManager;
    private FirestoreRepository repository;

    private static class CardData {
        int resId;
        int color;

        CardData(int resId, int color) {
            this.resId = resId;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CardData cardData = (CardData) o;
            return resId == cardData.resId && color == cardData.color;
        }

        @Override
        public int hashCode() {
            return 31 * resId + color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        authManager = FirebaseAuthManager.getInstance();
        repository = FirestoreRepository.getInstance();

        gridLayout = findViewById(R.id.gridLayout);
        tvScore = findViewById(R.id.tvScore);
        tvLevel = findViewById(R.id.tvLevel);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

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

        int rows, cols;
        switch (level) {
            case 1: rows = 2; cols = 2; break;
            case 2: rows = 3; cols = 2; break;
            case 3: rows = 4; cols = 2; break;
            case 4: rows = 4; cols = 3; break;
            case 5: rows = 4; cols = 4; break;
            case 6: rows = 5; cols = 4; break;
            case 7: rows = 6; cols = 4; break;
            case 8: rows = 6; cols = 5; break;
            case 9: rows = 6; cols = 6; break;
            case 10: rows = 7; cols = 6; break;
            default: rows = 4; cols = 4; break;
        }

        int totalCards = rows * cols;
        int pairsNeeded = totalCards / 2;

        cardDeck = new ArrayList<>();
        List<CardData> possiblePairs = new ArrayList<>();
        
        for (int res : cardResources) {
            for (int color : cardColors) {
                possiblePairs.add(new CardData(res, color));
            }
        }
        Collections.shuffle(possiblePairs);

        for (int i = 0; i < pairsNeeded; i++) {
            CardData pair = possiblePairs.get(i % possiblePairs.size());
            cardDeck.add(pair);
            cardDeck.add(pair);
        }
        Collections.shuffle(cardDeck);

        gridLayout.removeAllViews();
        gridLayout.setRowCount(rows);
        gridLayout.setColumnCount(cols);

        for (int i = 0; i < cardDeck.size(); i++) {
            ImageView imageView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            imageView.setLayoutParams(params);
            
            imageView.setBackgroundResource(R.drawable.game_card_back);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int padding = level > 7 ? 12 : 24;
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setId(i);
            imageView.setOnClickListener(this::onCardClicked);
            
            gridLayout.addView(imageView);
        }
    }

    private void onCardClicked(View v) {
        if (isProcessing) return;
        ImageView iv = (ImageView) v;
        if (flippedCards.contains(iv) || iv.getVisibility() == View.INVISIBLE) return;

        CardData data = cardDeck.get(iv.getId());
        flipCard(iv, data, () -> {
            flippedCards.add(iv);
            if (flippedCards.size() == 2) {
                checkMatch();
            }
        });
    }

    private void flipCard(ImageView iv, CardData data, Runnable onEnd) {
        Animation out = AnimationUtils.loadAnimation(this, R.anim.card_flip_left_out);
        out.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                iv.setImageResource(data.resId);
                iv.setColorFilter(data.color, PorterDuff.Mode.SRC_IN);
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
                    if (level < MAX_LEVEL) {
                        level++;
                        showSuccess("Level " + (level-1) + " Clear!");
                        new Handler(Looper.getMainLooper()).postDelayed(this::setupLevel, 1000);
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
                iv.setImageResource(0);
                iv.clearColorFilter();
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
