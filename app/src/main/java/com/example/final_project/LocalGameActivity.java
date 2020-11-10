package com.example.final_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.final_project.controller.Controller;
import com.example.final_project.entity.Node;
import com.example.final_project.entity.Player;
import com.example.final_project.entity.Values;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.final_project.MainActivity.musicBackgroundService;

public class LocalGameActivity extends AppCompatActivity {

    Node[][] board;
    Controller controller;

    TableLayout layout;
    ImageView avatar1, avatar2, currentChess;
    ProgressBar HP1, HP2;
    TextView name1, name2, timer1, timer2;
    int defaultColor = Values.black_chess;
    int hpLost = 0;
    int buttonEffect = R.raw.choose_sound;
    int playSound = R.raw.play_sound;


    int p = 5, s = 5;
    int time = p * 60 + s;

    private CountDownTimer mCountDownBlackTimer, mCountDownWhiteTimer;
    private boolean mBlackTimerRunning, mWhiteTimerRunning = false;
    private long mBlackTimeLeftInMillis = time * 1000,
            mWhiteTimeLeftInMillis = time * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_local_game);
        setting();
        Init();
        startBlackTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicBackgroundService != null) {
            musicBackgroundService.resumeMusic();
        }
    }

    void setting() {
        board = new Node[Values.board_size][Values.board_size];
        controller = new Controller(board);
        Bundle bundle = getIntent().getExtras();
        Player player1 = (Player) bundle.get("player1");
        Player player2 = (Player) bundle.get("player2");

        layout = findViewById(R.id.table);
        currentChess = findViewById(R.id.curentChess);
        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        HP1 = findViewById(R.id.HP1);
        HP2 = findViewById(R.id.HP2);
        name1 = findViewById(R.id.playerName1);
        name2 = findViewById(R.id.playerName2);
        timer1 = findViewById(R.id.textTime1);
        timer2 = findViewById(R.id.textTime2);

        HP1.setProgress(100);
        HP2.setProgress(100);
        name1.setText(player1.getName());
        name2.setText(player2.getName());
        avatar1.setImageResource(player1.getImgId());
        avatar2.setImageResource(player2.getImgId());
        currentChess.setImageResource(defaultColor);
    }

    void Init() {
        for (int i = 0; i < board.length; i++) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < board[0].length; j++) {
                final int x = i;
                final int y = j;
                final ImageButton button = new ImageButton(this);
                button.setImageResource(Values.chess_background_img);
                TableRow.LayoutParams layout = new TableRow.LayoutParams(110, 110);
                button.setLayoutParams(layout);
                final Node node = new Node(i, j, button, Values.valueEmpty, false);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (node.getValue() == Values.valueEmpty) {
                            MediaPlayer mPlayer = MediaPlayer.create(getApplication(), playSound);
                            mPlayer.start();
                            button.setImageResource(defaultColor);
                            node.setValues(defaultColor);
                            hpLost = controller.execute(x, y);
                            if (hpLost != 0) {
                                subHP();
                            }
                            if (defaultColor == Values.black_chess) {
                                pauseBlackTimer();
                                startWhiteTimer();
                            } else {
                                pauseWhiteTimer();
                                startBlackTimer();
                            }
                            defaultColor = defaultColor == Values.black_chess ? Values.white_chess : Values.black_chess;
                            currentChess.setImageResource(defaultColor);
                        }
                    }
                });
                board[i][j] = node;
                row.addView(button);
            }
            layout.addView(row);
        }
    }

    private void startBlackTimer() {
        mCountDownBlackTimer = new CountDownTimer(mBlackTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mBlackTimeLeftInMillis = millisUntilFinished;
                updateCountDownText(timer1, mBlackTimeLeftInMillis);
            }

            @Override
            public void onFinish() {
                mBlackTimerRunning = false;
            }
        }.start();
        mBlackTimerRunning = true;
    }

    private void startWhiteTimer() {
        mCountDownWhiteTimer = new CountDownTimer(mWhiteTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mWhiteTimeLeftInMillis = millisUntilFinished;
                updateCountDownText(timer2, mWhiteTimeLeftInMillis);
            }

            @Override
            public void onFinish() {
                mWhiteTimerRunning = false;
            }
        }.start();
        mWhiteTimerRunning = true;
    }

    private void pauseBlackTimer() {
        mCountDownBlackTimer.cancel();
        mBlackTimerRunning = false;
    }

    private void pauseWhiteTimer() {
        mCountDownWhiteTimer.cancel();
        mWhiteTimerRunning = false;
    }

    private void resetTimer() {
        mBlackTimeLeftInMillis = time;
        mWhiteTimeLeftInMillis = time;
        updateCountDownText(timer1, mBlackTimeLeftInMillis);
        updateCountDownText(timer2, mWhiteTimeLeftInMillis);
    }

    private void updateCountDownText(TextView textTimer, long timeLeft) {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textTimer.setText(timeLeftFormatted);
    }

    void changeSizeHP(ProgressBar pb) {
        pb.setProgress(pb.getProgress() - hpLost * 10);
    }

    void subHP() {
        if (defaultColor == Values.black_chess) {
            changeSizeHP(HP2);
        } else if (defaultColor == Values.white_chess) {
            changeSizeHP(HP1);
        }
    }

    public void newGameOnClick(View view) {
        MediaPlayer mPlayer = MediaPlayer.create(this, buttonEffect);
        mPlayer.start();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j].getButton().setImageResource(Values.chess_background_img);
                board[i][j].setValues(Values.valueEmpty);
            }
        }
        HP1.setProgress(100);
        HP2.setProgress(100);
        resetTimer();
    }

    public void quitOnClick(View view) {
        MediaPlayer mPlayer = MediaPlayer.create(this, buttonEffect);
        mPlayer.start();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}