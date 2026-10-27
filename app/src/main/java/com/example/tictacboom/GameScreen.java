package com.example.tictacboom;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class GameScreen extends AppCompatActivity {
    private final Button[][] cells = new Button[3][3]; // buttons on the screen
    private final char[][] board = new char[3][3]; // X, O, or blank
    private final int[][] taps = new int[3][3]; // Keeps track of how much the tile has been tapped
    private final TextView[][] tapCounterText = new TextView[3][3]; // Visual Text of Tap Count

    private View mainView;
    private int currentBgRes = R.drawable.gradientred;
    private char currentPlayer = 'X';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_screen);

        mainView = findViewById(R.id.main);
        mainView.setBackground(ContextCompat.getDrawable(this, currentBgRes));

        GridLayout grid = findViewById(R.id.grid);
        grid.setClipChildren(false);
        grid.setClipToPadding(false);

        int size = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._60sdp);
        int margin = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp);
        int textPadding = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c] = ' ';
                taps[r][c] = 0;

                // Container layout for each 3x3 cell
                FrameLayout cellContainer = new FrameLayout(this);
                cellContainer.setClipChildren(false);
                cellContainer.setClipToPadding(false);
                GridLayout.LayoutParams gridLP = new GridLayout.LayoutParams(
                        GridLayout.spec(r),
                        GridLayout.spec(c)
                );
                gridLP.width = size;
                gridLP.height = size;
                gridLP.setMargins(margin, margin, margin, margin);
                cellContainer.setLayoutParams(gridLP);

                // Button inside the cell
                Button buttonForBoard = new Button(this);
                buttonForBoard.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.intuit.sdp.R.dimen._24sdp));
                FrameLayout.LayoutParams buttonLP = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                buttonForBoard.setLayoutParams(buttonLP);

                final int rr = r, cc = c;
                buttonForBoard.setOnClickListener(v -> onTap(rr, cc));

                // Tap counter text overlay
                TextView tapCountText = new TextView(this);
                tapCountText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.intuit.ssp.R.dimen._10ssp));
                tapCountText.setElevation(10f);
                tapCountText.setPadding(0, 0, textPadding, textPadding);
                FrameLayout.LayoutParams tapCountLP = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.END
                );
                tapCountText.setLayoutParams(tapCountLP);

                // Assemble the cell view hierarchy
                cellContainer.addView(buttonForBoard);
                cellContainer.addView(tapCountText);
                grid.addView(cellContainer);

                cells[r][c] = buttonForBoard;
                tapCounterText[r][c] = tapCountText;
            }
        }
    }

    private void onTap(int r, int c) {
        boolean action = false;
        System.out.println(r + " " + c);
        if (board[r][c] == ' ') {
            board[r][c] = currentPlayer;
            cells[r][c].setText(String.valueOf(currentPlayer));
            taps[r][c] = 1;
            tapCounterText[r][c].setText(String.valueOf(taps[r][c]));
            action = true;
        } else if (board[r][c] == currentPlayer) {
            taps[r][c] += 1;
            tapCounterText[r][c].setText(String.valueOf(taps[r][c]));
            if (taps[r][c] == 3) {
                System.out.println("KABOOM");
                letterExplosion(cells[r][c], r, c);
            }
            action = true;
        }
        if (currentPlayer == 'X' && action) {
            currentPlayer = 'O';
            transitionBackgroundTo(R.drawable.gradientblue);
        } else if (currentPlayer == 'O' && action) {
            currentPlayer = 'X';
            transitionBackgroundTo(R.drawable.gradientred);
        }
    }

    private void transitionBackgroundTo(int newBgRes) {
        if (currentBgRes == newBgRes) return;
        Drawable from = ContextCompat.getDrawable(this, currentBgRes);
        Drawable to = ContextCompat.getDrawable(this, newBgRes);
        if (from != null && to != null) {
            TransitionDrawable transition = new TransitionDrawable(new Drawable[]{from, to});
            mainView.setBackground(transition);
            transition.startTransition(400); // 400ms cross-fade transition
        }
        currentBgRes = newBgRes;
    }

    private void letterExplosion(Button button, int r, int c) {
        // Bring button & cell container to top so explosion overlays neighboring cells
        if (button.getParent() instanceof View) {
            ((View) button.getParent()).bringToFront();
        }
        button.bringToFront();

        // Step 1: Shrink/charge up into small size (0.3x)
        button.animate()
                .scaleX(0.3f)
                .scaleY(0.3f)
                .setDuration(180)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // Step 2: Suddenly scale up HUGE (5.0x) and fade out
                    button.animate()
                            .scaleX(5.0f)
                            .scaleY(5.0f)
                            .alpha(0f)
                            .setDuration(200)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> {
                                // Step 3: Disappear then reappear at normal scale & opacity
                                button.setAlpha(1.0f);
                                button.setScaleX(1.0f);
                                button.setScaleY(1.0f);
                            })
                            .start();
                })
                .start();
    }

    private void checkWinner() {

    }
}