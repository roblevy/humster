package com.humster.humster;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GameCompleteActivity extends AppCompatActivity {

    private int mScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_complete);
        Intent intent = getIntent();
        mScore = intent.getIntExtra("score", 0);
        setScoreTextView();
    }

    public void setScoreTextView() {
        TextView scoreTextView = (TextView) findViewById(R.id.completeScoreTextView);
        scoreTextView.setText("Score: " + mScore);
    }

}
