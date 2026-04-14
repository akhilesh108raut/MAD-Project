package memgame.hemant.com.memorygame;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

import memgame.hemant.com.memorygame.application.MemoryGameApplication;
import memgame.hemant.com.memorygame.fragments.GameScoreFragment;
import memgame.hemant.com.memorygame.fragments.GameScreenFragment;
import memgame.hemant.com.memorygame.fragments.NavigationDrawerFragment;
import memgame.hemant.com.memorygame.iactivities.IMainActivity;
import memgame.hemant.com.memorygame.models.AllUsersRecord;
import memgame.hemant.com.memorygame.models.ErrorResponse;
import memgame.hemant.com.memorygame.presenters.MainActivityPresenter;
import memgame.hemant.com.memorygame.repositories.UserScoreData;
import memgame.hemant.com.memorygame.utils.IAppConstants;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, IMainActivity, GameScreenFragment.IGameScreenFragment {

    private static final String TAG = MainActivity.class.getName();
    public static final String ARG_SECTION_NUMBER = "section_number";

    private MemoryGameApplication mMemoryGameApp;
    private MainActivityPresenter mPresenter;
    private FragmentManager mFragmentManager;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private String mUserName;
    private int mUserScore;

    private GameScreenFragment mGameScreenFragment;
    private GameScoreFragment mGameScoreFragment;
    
    private TextView mLevelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadEnvironment();
        loadPresenter();
        loadHomeScreen();
    }

    private void loadEnvironment() {
        mMemoryGameApp = (MemoryGameApplication)getApplication();
    }

    private void loadPresenter() {
        mPresenter = (MainActivityPresenter)mMemoryGameApp.getPresenter(TAG);
        if(mPresenter == null) {
            mPresenter = new MainActivityPresenter(this);
            mMemoryGameApp.registerPresenter(TAG, mPresenter);
        }
    }

    private void loadHomeScreen() {
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0:
                mGameScreenFragment = GameScreenFragment.newInstance(position + 1);
                if(mFragmentManager == null) {
                    mFragmentManager = getSupportFragmentManager();
                }
                mFragmentManager.beginTransaction()
                        .replace(R.id.container, mGameScreenFragment)
                        .commit();
                break;
            case 1:
                mPresenter.getAllRecords();
                break;
        }
    }

    @Override
    public void showHighestScore() {
        mPresenter.queryHighestScore();
    }

    @Override
    public void notifyLevelChanged(int level) {
        if (mLevelTextView != null) {
            mLevelTextView.setText("Level: " + level);
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(false);
            
            if (actionBar.getCustomView() != null) {
                mLevelTextView = actionBar.getCustomView().findViewById(R.id.tv_level);
            }
        }
    }

    @Override
    public void notifyCurrentScore(int score) {
        mNavigationDrawerFragment.updateCurrentScore(score);
    }

    @Override
    public void showInputDialog(final int currentScore) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialogbox, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mUserName = editText.getText().toString();
                        mUserScore = currentScore;
                        mPresenter.saveRecord(mUserName, mUserScore, false);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onSuccessfulSaveData(String name, int score) {
        if (mGameScreenFragment != null) {
            mGameScreenFragment.resetGame();
        }
        mPresenter.queryHighestScore();
    }

    @Override
    public void onErrorReport(ErrorResponse errorResponse) {
        if(errorResponse.getErrorCode() == IAppConstants.ERROR_RESPONSE_DUPLICATE_ENTRY) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.duplicate_entry_title));
            alertDialogBuilder.setMessage(getResources().getString(R.string.duplicate_entry_message));
            alertDialogBuilder.setCancelable(false).
                    setPositiveButton(getResources().
                            getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mPresenter.saveRecord(mUserName, mUserScore, true);
                        }
                    }).setNegativeButton(getResources().getString(R.string.text_no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (mGameScreenFragment != null) {
                                mGameScreenFragment.resetGame();
                            }
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }

    public void goToScoreScreen(View view) {
        onNavigationDrawerItemSelected(1);
    }

    @Override
    protected void onDestroy() {
        exitActivity();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        exitActivity();
        super.onBackPressed();
    }

    private void exitActivity() {
        if(mFragmentManager != null) {
            mPresenter.clearFragmentBackstack(mFragmentManager);
        }
        mPresenter.unregisterPresenter();
        mMemoryGameApp.exitGame();
    }

    @Override
    public void onSuccessAllData(AllUsersRecord allUsersRecord) {
        if(allUsersRecord == null) {
            ArrayList<UserScoreData> list = new ArrayList<>();
            allUsersRecord = new AllUsersRecord();
            allUsersRecord.setUserScoreDataList(list);
        }
        mGameScoreFragment = GameScoreFragment.newInstance(2, allUsersRecord.getUserScoreDataList());
        if(mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        mFragmentManager.beginTransaction().replace(R.id.container, mGameScoreFragment).commit();
    }

    @Override
    public void updateHighestScore(int highestScore) {
        mNavigationDrawerFragment.setHighestScore(highestScore);
    }
}
