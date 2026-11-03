package com.example.tictacboom;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;

public class GameScreen extends AppCompatActivity {

    public enum Player {
        NONE, X, O
    }

    private final GifImageButton[][] cells = new GifImageButton[3][3]; // GIF image buttons on the screen
    private final FrameLayout[][] cellContainers = new FrameLayout[3][3]; // Cell tile containers
    private final Player[][] board = new Player[3][3]; // Player.X, Player.O, or Player.NONE
    private final int[][] taps = new int[3][3]; // Keeps track of how much the tile has been tapped
    private final TextView[][] tapCounterText = new TextView[3][3]; // Visual Text of Tap Count

    private View mainView;
    private View bgOverlay;
    private TextView turnIndicator;

    private int currentBgRes = R.drawable.gradientred;
    private Player currentPlayer = Player.X; // Variable keeping track of whether current player is X or O

    private final int xGifRes = R.drawable.x_anim; // x_anim.gif in res/drawable/
    private final int oGifRes = R.drawable.o_anim; // o_anim.gif in res/drawable/

    private final int oBombGIF = R.drawable.obomb_anim;

    private final int xBombGIF = R.drawable.xbomb_anim;

    private final int boomGIF = R.drawable.boom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        setContentView(R.layout.activity_game_screen);

        mainView = findViewById(R.id.main);
        bgOverlay = findViewById(R.id.bgOverlay);
        mainView.setBackground(ContextCompat.getDrawable(this, currentBgRes));

        turnIndicator = findViewById(R.id.turnIndicator);

        GridLayout grid = findViewById(R.id.grid);
        grid.setClipChildren(false);
        grid.setClipToPadding(false);

        int size = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._90sdp);
        int margin = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp);
        int textPadding = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c] = Player.NONE;
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

                // 1. Static Tile Background (tile.png stays still during explosions)
                ImageView tileBg = new ImageView(this);
                tileBg.setImageResource(R.drawable.tile);
                tileBg.setScaleType(ImageView.ScaleType.FIT_XY);
                FrameLayout.LayoutParams tileBgLP = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                tileBg.setLayoutParams(tileBgLP);

                // 2. GifImageButton for the GIF symbol overlay
                GifImageButton buttonForBoard = new GifImageButton(this);
                buttonForBoard.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Prevents native HWUI hardware renderer SIGABRT crash
                buttonForBoard.setScaleType(ImageView.ScaleType.FIT_CENTER);
                buttonForBoard.setAdjustViewBounds(true);
                buttonForBoard.setPadding(16, 16, 16, 16);
                buttonForBoard.setBackground(null); // Transparent background so tile.png shows through
                FrameLayout.LayoutParams buttonLP = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                buttonForBoard.setLayoutParams(buttonLP);

                final int rr = r, cc = c;
                buttonForBoard.setOnClickListener(v -> onTap(rr, cc));

                // 3. Tap counter text overlay
                TextView tapCountText = new TextView(this);
                tapCountText.setTextColor(Color.WHITE);
                tapCountText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.intuit.ssp.R.dimen._10ssp));
                tapCountText.setTypeface(ResourcesCompat.getFont(this, R.font.determination));
                tapCountText.setElevation(10f);
                tapCountText.setPadding(0, 0, textPadding, textPadding);
                FrameLayout.LayoutParams tapCountLP = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.END
                );
                tapCountText.setLayoutParams(tapCountLP);

                // Assemble the cell view hierarchy
                cellContainer.addView(tileBg);
                cellContainer.addView(buttonForBoard);
                cellContainer.addView(tapCountText);
                grid.addView(cellContainer);

                cellContainers[r][c] = cellContainer;
                cells[r][c] = buttonForBoard;
                tapCounterText[r][c] = tapCountText;
            }
        }
    }

    private void onTap(int r, int c) {
        boolean action = false;
        System.out.println(r + " " + c);
        tileTapAnimation(cellContainers[r][c]);
        if (board[r][c] == Player.NONE) {
            board[r][c] = currentPlayer;
            setCellGif(cells[r][c], currentPlayer == Player.X ? xGifRes : oGifRes);
            taps[r][c] = 1;
            tapCounterText[r][c].setText(String.valueOf(taps[r][c]));
            action = true;
        } else if (board[r][c] == currentPlayer) {
            taps[r][c] += 1;
            tapCounterText[r][c].setText(String.valueOf(taps[r][c]));
            if (taps[r][c] == 3) {
                System.out.println("KABOOM");
                setCellGif(cells[r][c], boomGIF);
                letterExplosion(cells[r][c], r, c);
            } else {
               setCellGif(cells[r][c], currentPlayer == Player.X ? xBombGIF : oBombGIF);
            }
            action = true;
        }
        if (currentPlayer == Player.X && action) {
            turnIndicator.setText("O's turn");
            currentPlayer = Player.O;
            transitionBackgroundTo(R.drawable.gradientblue);
        } else if (currentPlayer == Player.O && action) {
            turnIndicator.setText("X's turn");
            currentPlayer = Player.X;
            transitionBackgroundTo(R.drawable.gradientred);
        }
    }

    private void setCellGif(GifImageButton button, int resId) {
        try {
            GifDrawable gifDrawable = new GifDrawable(getResources(), resId);
            button.setImageDrawable(gifDrawable);
        } catch (Exception e) {
            button.setImageResource(resId);
        }
    }

    private void transitionBackgroundTo(int newBgRes) {
        if (currentBgRes == newBgRes) return;
        bgOverlay.setBackground(ContextCompat.getDrawable(this, newBgRes));
        bgOverlay.animate()
                .alpha(1.0f)
                .setDuration(300)
                .withEndAction(() -> {
                    mainView.setBackground(ContextCompat.getDrawable(this, newBgRes));
                    bgOverlay.setAlpha(0f);
                })
                .start();
        currentBgRes = newBgRes;
    }

    private void letterExplosion(GifImageButton button, int r, int c) {
        if (button.getParent() instanceof View) {
            ((View) button.getParent()).bringToFront();
        }
        button.bringToFront();

        button.animate()
                .scaleX(5.0f)
                .scaleY(5.0f)
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    board[r][c] = Player.NONE;
                    taps[r][c] = 0;

                    Drawable d = button.getDrawable();
                    if (d instanceof GifDrawable) {
                        ((GifDrawable) d).recycle();
                    }
                    button.setImageDrawable(null);
                    tapCounterText[r][c].setText("");

                    button.setAlpha(1.0f);
                    button.setScaleX(1.0f);
                    button.setScaleY(1.0f);
                })
                .start();
    }

    private void tileTapAnimation(View cellView) {
        cellView.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(80)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    cellView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(80)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                })
                .start();
    }

    private void checkWinner() {

    }
}