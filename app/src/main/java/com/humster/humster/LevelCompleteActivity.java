package com.humster.humster;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LevelCompleteActivity extends AppCompatActivity {

    int mLevelCompleted;
    int mScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_complete);

        Intent intent = getIntent();
        mLevelCompleted = intent.getIntExtra("levelNumber", -1);
        mScore = intent.getIntExtra("score", -1);
        setLevelCompletedTextView();
        setScoreTextView();
    }

    public void setLevelCompletedTextView() {
        TextView levelNumberTextView = (TextView) findViewById(R.id.levelTextView);
        levelNumberTextView.setText("" + mLevelCompleted);
    }

    public void setScoreTextView() {
        TextView scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        scoreTextView.setText("Score: " + mScore);
    }

    public void onClickNextLevel(View v) {
        Intent nextLevelIntent = new Intent(this, GameActivity.class);
        nextLevelIntent.putExtra("levelNumber", mLevelCompleted + 1);
        nextLevelIntent.putExtra("score", mScore);
        startActivity(nextLevelIntent);
    }

}
