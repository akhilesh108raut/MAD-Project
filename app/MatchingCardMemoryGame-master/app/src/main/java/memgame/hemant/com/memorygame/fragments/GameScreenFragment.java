package memgame.hemant.com.memorygame.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import memgame.hemant.com.memorygame.MainActivity;
import memgame.hemant.com.memorygame.R;

/**
 * Game screen fragment design on HomeScreen
 */
public class GameScreenFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = GameScreenFragment.class.getName();

    private View mRootView;
    private TableLayout cardsTableLayout;

    private final static int[] cardDeckResources = new int[]{
            R.drawable.game_tile_1, R.drawable.game_tile_2, R.drawable.game_tile_3, R.drawable.game_tile_4,
            R.drawable.game_tile_5, R.drawable.game_tile_6, R.drawable.game_tile_7, R.drawable.game_tile_8,
            R.drawable.game_tile_1, R.drawable.game_tile_2, R.drawable.game_tile_3, R.drawable.game_tile_4,
            R.drawable.game_tile_5, R.drawable.game_tile_6, R.drawable.game_tile_7, R.drawable.game_tile_8,
    };

    private int[] currentCardDeck = new int[16];
    private ArrayList<ImageView> flipTracker;
    private IGameScreenFragment iActivityNotifier;

    private int currentScore = 0;
    private int imageRemaining = 16;
    private int currentLevel = 1;
    private int matchesInLevel = 0;

    public static GameScreenFragment newInstance(int sectionNumber) {
        GameScreenFragment fragment = new GameScreenFragment();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public GameScreenFragment() {
    }

    public void resetGame() {
        currentScore = 0;
        currentLevel = 1;
        startLevel(currentLevel);
    }

    private void startLevel(int level) {
        imageRemaining = 16;
        matchesInLevel = 0;
        flipTracker.clear();
        shuffleAllCards();
        
        if (mRootView != null) {
            updateUIForLevel();
        }
        
        if (iActivityNotifier != null) {
            iActivityNotifier.notifyLevelChanged(level);
            iActivityNotifier.notifyCurrentScore(currentScore);
        }
    }

    private void updateUIForLevel() {
        for (int i = 0; i < cardsTableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) cardsTableLayout.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                ImageView imageView = (ImageView) row.getChildAt(j);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.game_card_back);
            }
        }
    }

    public interface IGameScreenFragment {
        void notifyCurrentScore(int score);
        void showInputDialog(int score);
        void showHighestScore();
        void notifyLevelChanged(int level);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            ((MainActivity) context).onSectionAttached(
                    getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
        }
        iActivityNotifier = (IGameScreenFragment) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        cardsTableLayout = mRootView.findViewById(R.id.tl_cardsgrid);

        for (int i = 0; i < cardsTableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) cardsTableLayout.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                ImageView imageView = (ImageView) row.getChildAt(j);
                imageView.setId(4 * i + j);
                imageView.setOnClickListener(this);
            }
        }

        flipTracker = new ArrayList<>(2);
        startLevel(currentLevel);
        iActivityNotifier.showHighestScore();
        return mRootView;
    }

    @Override
    public void onClick(View v) {
        if (flipTracker.size() == 2) return;
        
        final ImageView iv = (ImageView) v;
        if (iv.getVisibility() != View.VISIBLE || flipTracker.contains(iv)) return;

        Animation animationOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.card_flip_left_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                iv.setImageResource(currentCardDeck[iv.getId()]);
                Animation animationIn = AnimationUtils.loadAnimation(requireActivity(), R.anim.card_flip_left_in);
                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        flipTracker.add(iv);
                        if (flipTracker.size() == 2) compareViews();
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                iv.startAnimation(animationIn);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        iv.startAnimation(animationOut);
    }

    public void shuffleAllCards() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int res : cardDeckResources) list.add(res);
        Collections.shuffle(list);
        for (int i = 0; i < list.size(); i++) currentCardDeck[i] = list.get(i);
    }

    private void compareViews() {
        final ImageView view1 = flipTracker.get(0);
        final ImageView view2 = flipTracker.get(1);
        
        // DIFFICULTY: Flip back time gets shorter as levels progress
        // Level 1: 1000ms, Level 10: 100ms
        long dynamicDelay = Math.max(100, 1100 - (currentLevel * 100));

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                if (view1.getDrawable().getConstantState().equals(view2.getDrawable().getConstantState())) {
                    view1.setVisibility(View.INVISIBLE);
                    view2.setVisibility(View.INVISIBLE);
                    flipTracker.clear();
                    matchesInLevel++;
                    currentScore += (10 * currentLevel); 
                    imageRemaining -= 2;
                    
                    if (imageRemaining == 0) {
                        if (currentLevel < 10) {
                            currentLevel++;
                            startLevel(currentLevel);
                        } else {
                            iActivityNotifier.showInputDialog(currentScore);
                        }
                    }
                } else {
                    flipBackCard(view1);
                    flipBackCard(view2);
                    // Penalty increases with level
                    currentScore = Math.max(0, currentScore - (2 * currentLevel));
                }
                iActivityNotifier.notifyCurrentScore(currentScore);
            }
        }, dynamicDelay);
    }

    private void flipBackCard(final ImageView cardView) {
        Animation animOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.card_flip_left_out);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setImageResource(R.drawable.game_card_back);
                Animation animIn = AnimationUtils.loadAnimation(requireActivity(), R.anim.card_flip_left_in);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (flipTracker.contains(cardView)) flipTracker.remove(cardView);
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                });
                cardView.startAnimation(animIn);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        cardView.startAnimation(animOut);
    }
}
